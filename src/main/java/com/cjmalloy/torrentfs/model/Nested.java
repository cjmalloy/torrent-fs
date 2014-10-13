package com.cjmalloy.torrentfs.model;

import com.cjmalloy.torrentfs.util.JsonUtil.Factory;
import com.cjmalloy.torrentfs.util.JsonUtil.HasJson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.turn.ttorrent.common.Torrent;


public class Nested implements HasJson
{
    public static final Factory<Nested> FACTORY = new Factory<Nested>()
    {
        @Override
        public Nested create()
        {
            return new Nested();
        }
    };

    public Encoding encoding;
    public String mount;
    public JsonElement torrent;

    public Torrent getTorrent()
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Nested parseJson(JsonObject o)
    {
        if (o.has("encoding"))
        {
            encoding = getEncoding(o.get("encoding").getAsString());
        }
        if (o.has("mount"))
        {
            mount = o.get("mount").getAsString();
        }
        if (o.has("torrent"))
        {
            torrentSource = o.get("torrent");
        }
        return this;
    }

    private static Encoding getEncoding(String enc)
    {
        if (enc.equalsIgnoreCase("bencode")) return Encoding.BENCODE_BASE64;
        if (enc.equalsIgnoreCase("magnet")) return Encoding.MAGNET;
        if (enc.equalsIgnoreCase("json")) return Encoding.JSON;
        return null;
    }

    public enum Encoding
    {
        BENCODE_BASE64,
        MAGNET,
        JSON;
    }
}
