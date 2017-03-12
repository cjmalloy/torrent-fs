package com.cjmalloy.torrentfs.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonWriter;


public class JsonUtil {
  public static <T extends HasJson> List<T> parseList(JsonArray n, Factory<T> factory) {
    List<T> l = new ArrayList<T>();
    for (int i = 0; i < n.size(); i++) {
      T t = factory.create();
      t.parseJson(n.get(i).getAsJsonObject());
      l.add(t);
    }
    return l;
  }

  public static List<String> parseStringList(JsonArray n) {
    List<String> l = new ArrayList<String>();
    for (int i = 0; i < n.size(); i++) {
      l.add(n.get(i).getAsString());
    }
    return l;
  }

  public static String prettyPrint(JsonElement json) {
    try {
      StringWriter stringWriter = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(stringWriter);
      jsonWriter.setLenient(true);
      jsonWriter.setIndent("    ");
      Streams.write(json, jsonWriter);
      return stringWriter.toString();
    } catch (IOException e) {
      throw new AssertionError(e);
    }
  }

  public static JsonArray writeList(List<? extends HasJson> nested) {
    JsonArray a = new JsonArray();
    for (HasJson j : nested) {
      a.add(j.writeJson());
    }
    return a;
  }

  public static JsonElement writeStringList(Collection<String> l) {
    JsonArray a = new JsonArray();
    for (String s : l) {
      a.add(new JsonPrimitive(s));
    }
    return a;
  }

  public interface Factory<T> {
    T create();
  }

  public interface HasJson {
    HasJson parseJson(JsonObject o);

    JsonElement writeJson();
  }
}
