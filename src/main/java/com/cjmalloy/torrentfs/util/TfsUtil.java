package com.cjmalloy.torrentfs.util;

import java.util.List;
import java.util.Map;

import com.turn.ttorrent.bcodec.BEValue;
import com.turn.ttorrent.bcodec.InvalidBEncodingException;

public class TfsUtil 
{
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

}
