package com.cjmalloy.torrentfs.server;

import java.net.Inet4Address;

import com.cjmalloy.torrentfs.TfsClient;


public class TfsClientSingleton
{
    private static TfsClient instance;

    public static void init(Inet4Address address, String rootPath)
    {
        instance = new TfsClient(address, rootPath);
    }

    public static TfsClient get()
    {
        return instance;
    }
}
