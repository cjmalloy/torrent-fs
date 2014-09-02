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

        ServletHolder sh = new ServletHolder(ServletContainer.class);
        sh.setInitParameter("com.sun.jersey.config.property.resourceConfigClass", "com.sun.jersey.api.core.PackagesResourceConfig");
        sh.setInitParameter("com.sun.jersey.config.property.packages", "com.cjmalloy.torrentfs.server.remote.rest");
        sh.setInitParameter("com.sun.jersey.api.json.POJOMappingFeature", "true");

        Server server = new Server(port);
        ServletContextHandler context = new ServletContextHandler(server, "/", ServletContextHandler.SESSIONS);
        context.addServlet(sh, "/*");
        try
        {
        	System.out.println("Starting server...");
			server.start();
	        server.join();
		}
        catch (Exception e)
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
