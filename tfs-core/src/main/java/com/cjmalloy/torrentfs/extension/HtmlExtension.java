package com.cjmalloy.torrentfs.extension;

import com.google.gson.JsonObject;


public class HtmlExtension implements Extension
{
    public String index;

    @Override
    public HtmlExtension parseJson(JsonObject o)
    {
        if (o.has("index"))
        {
            index = o.get("index").getAsString();
        }
        return this;
    }
}
