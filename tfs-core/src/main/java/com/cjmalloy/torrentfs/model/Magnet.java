package com.cjmalloy.torrentfs.model;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.turn.ttorrent.common.Torrent;

public class Magnet
{

    public String infoHash = null;
    public String name = null;
    public List<URI> announce = null;

    public Magnet load(Torrent torrent) throws UnsupportedEncodingException
    {
        infoHash = torrent.getHexInfoHash();
        name = torrent.getName();
        announce = new ArrayList<>();
        for (List<URI> uris : torrent.getAnnounceList())
        for (URI uri : uris)
        {
            announce.add(uri);
        }
        return this;
    }

    public Torrent createTorrent()
    {
        //TODO:
        return null;
    }

    public Magnet parse(String magnet_uri) throws ParseException, UnsupportedEncodingException, URISyntaxException
    {
        if (!magnet_uri.startsWith("magnet:?")) throw new ParseException(magnet_uri, 0);
        magnet_uri = magnet_uri.substring(8);
        String[] args = magnet_uri.split("&");
        for (String arg : args)
        {
            if (arg.startsWith("xt=urn:btih:")) infoHash = arg.substring(12);
            else if (arg.startsWith("dn=")) name = URLDecoder.decode(arg.substring(3), "UTF-8");
            else if (arg.startsWith("tr=")) addAnnounce(new URI(URLDecoder.decode(arg.substring(3), "UTF-8")));
        }
        return this;
    }

    public void addAnnounce(URI uri)
    {
        if (announce == null) announce = new ArrayList<>();
        announce.add(uri);
    }

    public String write() throws UnsupportedEncodingException
    {
        String magnet = "magnet:?xt=urn:btih:" + infoHash
                      + "&dn=" + URLEncoder.encode(name, "UTF-8");
        for (URI uri : announce)
        {
            magnet += "&tr=" + URLEncoder.encode(uri.toString(), "UTF-8");
        }
        return magnet;
    }
}
