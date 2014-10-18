package com.cjmalloy.torrentfs.util;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class JsonUtil
{
    public static <T extends HasJson> List<T> parseList(JsonArray n, Factory<T> factory)
    {
        List<T> l = new ArrayList<T>();
        for (int i=0; i<n.size(); i++)
        {
            T t = factory.create();
            t.parseJson(n.get(i).getAsJsonObject());
            l.add(t);
        }
        return l;
    }

    public static List<String> parseStringList(JsonArray n)
    {
        List<String> l = new ArrayList<String>();
        for (int i=0; i<n.size(); i++)
        {
            l.add(n.get(i).getAsString());
        }
        return l;
    }

    public interface Factory<T>
    {
        T create();
    }

    public interface HasJson
    {
        HasJson parseJson(JsonObject o);
    }
}
