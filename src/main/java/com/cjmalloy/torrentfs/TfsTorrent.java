package com.cjmalloy.torrentfs;

import java.util.List;

import com.cjmalloy.torrentfs.model.Meta;
import com.turn.ttorrent.client.Client;


public class TfsTorrent
{
    public Client client;
    public String infoHash;
    public Meta tfsData = null;
    public List<TfsTorrent> nested;

    public TfsTorrent(Client c, String hash)
    {
        this.client = c;
        this.infoHash = hash;
    }
}
