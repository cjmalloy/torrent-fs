package com.cjmalloy.torrentfs.extension;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class GpgExtension implements Extension
{

    public static final String JSON_ID = "gpg";

    @Override
    public String jsonId()
    {
        return JSON_ID;
    }

    @Override
    public GpgExtension parseJson(JsonObject o)
    {
        // TODO Auto-generated method stub
        return this;
    }

    @Override
    public JsonElement writeJson()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
