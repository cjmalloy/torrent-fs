package com.cjmalloy.torrentfs.model;

import java.io.IOException;

import com.cjmalloy.torrentfs.util.JsonUtil.Factory;
import com.cjmalloy.torrentfs.util.JsonUtil.HasJson;
import com.cjmalloy.torrentfs.util.TfsUtil;
import com.cjmalloy.torrentfs.util.TfsUtil.Encoding;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
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

    public transient Meta meta = null;

    public Torrent getTorrent() throws IOException
    {
        return TfsUtil.getTorrentFromJson(encoding, torrent);
    }

    public Nested parseJson(JsonObject o)
    {
        if (o.has("encoding"))
        {
            encoding = Encoding.getEncoding(o.get("encoding").getAsString());
        }
        if (o.has("mount"))
        {
            mount = o.get("mount").getAsString();
        }
        if (o.has("torrent"))
        {
            torrent = o.get("torrent");
        }
        return this;
    }

    @Override
    public JsonElement writeJson()
    {
        JsonObject o = new JsonObject();
        o.add("encoding", new JsonPrimitive(encoding.toString()));
        o.add("mount", new JsonPrimitive(mount));
        o.add("torrent", torrent);
        return o;
    }
}
