package com.cjmalloy.torrentfs.server;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Arrays;
import java.util.Enumeration;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AllowSymLinkAliasChecker;
import org.eclipse.jetty.server.handler.ContextHandler.AliasCheck;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;
import org.teleal.cling.UpnpService;
import org.teleal.cling.UpnpServiceImpl;
import org.teleal.cling.support.igd.PortMappingListener;
import org.teleal.cling.support.model.PortMapping;

import com.turn.ttorrent.client.ConnectionHandler;

public class Entry
{
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args)
    {
        int port = DEFAULT_PORT;
        String tfsCache = "./tfs/";
        if (args.length == 2)
        {
            tfsCache = args[0];
            port = Integer.parseInt(args[1]);
        }
        else if (args.length == 1)
        {
            tfsCache = args[0];
        }
        else if (args.length != 0)
        {
            System.out.println("Usage: torrent-fs [DIR [PORT]]");
            return;
        }

        if (!tfsCache.endsWith("/")) tfsCache += "/";
        Inet4Address address;
        try
        {
            address = getIPv4Address("eth0");
            TfsClientSingleton.init(address, tfsCache);
        }
        catch (UnknownHostException|SocketException e)
        {
            throw new Error(e);
        }

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);

        // Rest API
        ServletHolder rest = context.addServlet(ServletContainer.class, "/*");
        rest.setInitOrder(1);
        rest.setInitParameter("jersey.config.server.provider.packages","com.cjmalloy.torrentfs.server.remote.rest");

        // Static Files (needed for the html extension)
        ServletHolder html = new ServletHolder(DefaultServlet.class);
        html.setInitParameter("resourceBase", tfsCache);
        html.setInitParameter("dirAllowed", "true");
        html.setInitParameter("pathInfoOnly", "true");
        context.addServlet(html,"/ext/html/static/*");
        context.setAliasChecks(Arrays.asList((AliasCheck) new AllowSymLinkAliasChecker()));

        //uPnP
        PortMapping desiredMapping = new PortMapping(
                ConnectionHandler.PORT_RANGE_START,
                address.getHostAddress(),
                PortMapping.Protocol.TCP
        );

        UpnpService upnpService =
                new UpnpServiceImpl(
                        new PortMappingListener(desiredMapping)
                );

        upnpService.getControlPoint().search();

        try
        {
            server.start();
            System.out.println("Server started on port " + port);
            server.join();
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
    }

    private static Inet4Address getIPv4Address(String iface) throws SocketException, UnsupportedAddressTypeException, UnknownHostException
    {
        if (iface != null)
        {
            Enumeration<InetAddress> addresses = NetworkInterface.getByName(iface).getInetAddresses();
            while (addresses.hasMoreElements())
            {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address) { return (Inet4Address) addr; }
            }
        }

        InetAddress localhost = InetAddress.getLocalHost();
        if (localhost instanceof Inet4Address) { return (Inet4Address) localhost; }

        throw new UnsupportedAddressTypeException();
    }

}
