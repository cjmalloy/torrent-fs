package com.cjmalloy.torrentfs.extension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cjmalloy.torrentfs.util.JsonUtil.HasJson;
import com.google.gson.JsonObject;


public interface Extension extends HasJson
{
    @Override
    Extension parseJson(JsonObject o);

    public static class ExtensionFactory
    {
        public static Map<String, Extension> load(JsonObject meta, List<String> l)
        {
            Map<String, Extension> ret = new HashMap<>();
            for (String ext : l)
            {
                Extension extImpl = getExt(ext);
                // Unsupported extension
                if (extImpl == null) continue;

                JsonObject extObj = meta.get(ext).getAsJsonObject();
                ret.put(ext, extImpl.parseJson(extObj));
            }
            return ret;
        }

        private static Extension getExt(String ext)
        {
            switch (ext)
            {
            case "gpg":      return new GpgExtension();
            case "html":     return new HtmlExtension();
            case "revision": return new RevisionExtension();
            }
            return null;
        }
    }
}
