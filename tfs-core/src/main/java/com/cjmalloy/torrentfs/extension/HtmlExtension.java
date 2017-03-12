package com.cjmalloy.torrentfs.extension;

import com.google.gson.*;


public class HtmlExtension implements Extension {
  public static final String JSON_ID = "html";

  public String index;

  @Override
  public String jsonId() {
    return JSON_ID;
  }

  @Override
  public HtmlExtension parseJson(JsonObject o) {
    if (o.has("index")) {
      index = o.get("index").getAsString();
    }
    return this;
  }

  @Override
  public JsonElement writeJson() {
    JsonObject o = new JsonObject();
    if (index != null) {
      o.add("index", new JsonPrimitive(index));
    }
    return o;
  }
}
