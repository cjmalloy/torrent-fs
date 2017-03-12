package com.cjmalloy.torrentfs.extension;

import java.util.*;

import com.cjmalloy.torrentfs.util.JsonUtil.HasJson;
import com.google.gson.JsonObject;


public interface Extension extends HasJson {
  String jsonId();

  @Override
  Extension parseJson(JsonObject o);

  public static class ExtensionFactory {
    public static Map<String, Extension> load(JsonObject meta, List<String> l) {
      Map<String, Extension> ret = new HashMap<>();
      for (String ext : l) {
        Extension extImpl = getExt(ext);
        // Unsupported extension
        if (extImpl == null) continue;

        JsonObject extObj = meta.get(ext).getAsJsonObject();
        ret.put(ext, extImpl.parseJson(extObj));
      }
      return ret;
    }

    private static Extension getExt(String ext) {
      switch (ext) {
        case GpgExtension.JSON_ID:
          return new GpgExtension();
        case HtmlExtension.JSON_ID:
          return new HtmlExtension();
        case RevisionExtension.JSON_ID:
          return new RevisionExtension();
      }
      return null;
    }
  }
}
