package com.cjmalloy.torrentfs.server;

import jargs.gnu.CmdLineParser;

import java.io.PrintStream;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Arrays;
import java.util.Enumeration;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
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
    public static final String DEFAULT_CACHE = "./tfs/";
    public static final boolean DEFAULT_HTML_STATIC = false;
    public static final boolean DEFAULT_UPNP = true;

    public static void main(String[] args)
    {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%d [%-25t] %-5p: %m%n")));

        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option argHelp        = parser.addBooleanOption('h', "help");
        CmdLineParser.Option argPort        = parser.addIntegerOption('p', "port");
        CmdLineParser.Option argCache       = parser.addStringOption ('d', "cache-dir");
        CmdLineParser.Option argHtmlStatic  = parser.addBooleanOption('s', "html-static");
        CmdLineParser.Option argUpnp        = parser.addBooleanOption('u', "upnp");

        try
        {
            parser.parse(args);
        }
        catch (CmdLineParser.OptionException oe)
        {
            System.err.println(oe.getMessage());
            usage(System.err);
            System.exit(1);
        }

        try
        {
            parser.parse(args);
        }
        catch (CmdLineParser.OptionException oe)
        {
            System.err.println(oe.getMessage());
            usage(System.err);
            System.exit(1);
        }

        int port = (int) parser.getOptionValue(argPort, DEFAULT_PORT);
        String tfsCache = (String) parser.getOptionValue(argCache, DEFAULT_CACHE);
        boolean htmlStatic = (boolean) parser.getOptionValue(argHtmlStatic, DEFAULT_HTML_STATIC);
        boolean upnp = (boolean) parser.getOptionValue(argUpnp, DEFAULT_UPNP);

        // Display help and exit if requested
        if (Boolean.TRUE.equals((Boolean)parser.getOptionValue(argHelp))) {
            usage(System.out);
            System.exit(0);
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
        if (htmlStatic)
        {
            ServletHolder html = new ServletHolder(DefaultServlet.class);
            html.setInitParameter("resourceBase", tfsCache);
            html.setInitParameter("dirAllowed", "true");
            html.setInitParameter("pathInfoOnly", "true");
            context.addServlet(html,"/ext/html/static/*");
            context.setAliasChecks(Arrays.asList((AliasCheck) new AllowSymLinkAliasChecker()));
        }

        //uPnP
        if (upnp)
        {
            PortMapping desiredMapping = new PortMapping(
                ConnectionHandler.PORT_RANGE_START,
                address.getHostAddress(),
                PortMapping.Protocol.TCP
            );

            UpnpService upnpService = new UpnpServiceImpl(
                new PortMappingListener(desiredMapping)
            );

            upnpService.getControlPoint().search();
        }

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

    /**
     * Display program usage on the given {@link PrintStream}.
     */
    private static void usage(PrintStream s) {
        s.println("Usage: torrent-fs [OPTIONS]");
        s.println();
        s.println("Available options:");
        s.println("  -h,--help                  Show this help and exit");
        s.println("  -p,--port PORT             Listen on port " + DEFAULT_PORT);
        s.println("  -d,--cache-dir DIR         Cache torrents in directory DIR (Defaults " + DEFAULT_CACHE + ")");
        s.println("  -s,--html-static           Serve the cache dir as /ext/html/static");
        s.println("  -u,--upnp                  Enable UPnP");
        s.println();
    }

}
