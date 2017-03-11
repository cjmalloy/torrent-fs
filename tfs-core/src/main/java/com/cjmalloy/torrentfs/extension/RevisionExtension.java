package com.cjmalloy.torrentfs.extension;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


public class RevisionExtension implements Extension {

  public static final String JSON_ID = "revision";

  @Override
  public String jsonId() {
    return JSON_ID;
  }

  @Override
  public RevisionExtension parseJson(JsonObject o) {
    // TODO Auto-generated method stub
    return this;
  }

  @Override
  public JsonElement writeJson() {
    // TODO Auto-generated method stub
    return null;
  }

}
