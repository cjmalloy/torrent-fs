package com.cjmalloy.torrentfs.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class Entry
{
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args)
    {
        int port = DEFAULT_PORT;
        String tfsCache;
        if (args.length == 2)
        {
            tfsCache = args[0];
            port = Integer.parseInt(args[1]);
        }
        else if (args.length == 1)
        {
            tfsCache = args[0];
        }
        else
        {
            System.out.println("Usage: torrent-fs DIR [PORT]");
            return;
        }

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);

        // Rest API
        ServletHolder rest = context.addServlet(ServletContainer.class, "/*");
        rest.setInitOrder(1);
        rest.setInitParameter("jersey.config.server.provider.packages","com.cjmalloy.torrentfs.server.remote.rest");

        // Static files
        ServletHolder staticFiles = context.addServlet(DefaultServlet.class, "/tfs/");
        staticFiles.setInitOrder(2);
        staticFiles.setInitParameter("resourceBase", tfsCache);
        staticFiles.setInitParameter("dirAllowed", "true");

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

}
