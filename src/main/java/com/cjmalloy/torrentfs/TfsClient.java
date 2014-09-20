package com.cjmalloy.torrentfs;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;


public class TfsClient
{
    private String rootPath = null;
    private Inet4Address address;
    private Map<String, Client> lookup = new HashMap<String, Client>();

    public TfsClient(Inet4Address address, String rootPath)
    {
        this.address = address;
        this.rootPath = rootPath;
    }

    public Client addTorrent(Torrent torrent) throws UnknownHostException, IOException
    {
        String infoHash = torrent.getHexInfoHash();
        if (lookup.containsKey(infoHash)) return null;

        File f = new File(rootPath + infoHash + "/");
        f.mkdirs();
        Client c = new Client(address, new SharedTorrent(torrent, f));
        lookup.put(infoHash, c);
        c.share();
        return c;
    }

    public void printInfo()
    {
        for (Client c : lookup.values())
        {
            c.info();
        }
    }
}
