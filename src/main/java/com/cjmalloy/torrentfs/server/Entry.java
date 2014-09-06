package com.cjmalloy.torrentfs.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.servlet.ServletContainer;

public class Entry
{
    public static final int DEFAULT_PORT = 8080;

    public static void main(String[] args)
    {
        int port = DEFAULT_PORT;
        if (args.length == 1)
        {
            port = Integer.parseInt(args[0]);
        }
        else if (args.length != 0)
        {
            System.out.println("Usage: torrent-fs [PORT]");
            return;
        }

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        ServletHolder sh = context.addServlet(ServletContainer.class, "/*");
        sh.setInitOrder(1);
        sh.setInitParameter("jersey.config.server.provider.packages","com.cjmalloy.torrentfs.server.remote.rest");
        try
        {
            server.start();
            System.out.println("Server started on port " + port);
            server.join();
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
