package com.cjmalloy.torrentfs.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.glassfish.jersey.internal.util.Base64;

import com.google.gson.JsonElement;
import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;
import com.turn.ttorrent.common.Torrent;

public class TfsUtil
{
    public static Torrent getTorrentFromBencode(String torrent_base64) throws IOException
    {
        return new Torrent(Base64.decode(torrent_base64.getBytes(Charset.forName("UTF-8"))), false);
    }

    public static Torrent getTorrentFromJson(Encoding encoding, JsonElement torrent) throws IOException
    {
        switch (encoding)
        {
        case BENCODE_BASE64:
            return getTorrentFromBencode(torrent.getAsString());
        case JSON:
            return null; //TODO
        case MAGNET:
            return null; //TODO
        }
        return null;
    }

    public static String printList(List<BEValue> value) throws InvalidBEncodingException
    {
        String s = "[";
        boolean started = false;
        for (BEValue v : value)
        {
            if (started) s += ",";
            started = true;
            s += printValue(v);
        }
        return s + "]";
    }

    public static String printMap(Map<String, BEValue> value) throws InvalidBEncodingException
    {
        String s = "{";
        boolean started = false;
        for (String k : value.keySet())
        {
            if (started) s += ",";
            started = true;
            s += "\"" + k + "\": ";
            if (k.equals("pieces"))
            {
                s += "\"snip\"";
            }
            else
            {
                s += printValue(value.get(k));
            }
        }
        return s + "}";
    }

    public static String printValue(BEValue value) throws InvalidBEncodingException
    {
        if (value.getValue() instanceof byte[])
        {
            return "\"" + value.getString("UTF-8") + "\"";
        }
        else if (value.getValue() instanceof Integer)
        {
            return ""+value.getInt();
        }
        else if (value.getValue() instanceof Long)
        {
            return ""+value.getLong();
        }
        else if (value.getValue() instanceof Number)
        {
            return ""+value.getNumber();
        }
        else if (value.getValue() instanceof Map)
        {
            return printMap(value.getMap());
        }
        else if (value.getValue() instanceof List)
        {
            return printList(value.getList());
        }
        return "{}";
    }

    public enum Encoding
    {
        BENCODE_BASE64("bencode"),
        MAGNET("magnet"),
        JSON("json");

        private String strValue;

        Encoding(String strValue)
        {
            this.strValue = strValue;
        }

        @Override
        public String toString()
        {
            return strValue;
        }

        public static Encoding getEncoding(String strValue)
        {
            for (Encoding enc : Encoding.values())
            {
                if (enc.toString().equalsIgnoreCase(strValue)) return enc;
            }
            return null;
        }
    }
}
