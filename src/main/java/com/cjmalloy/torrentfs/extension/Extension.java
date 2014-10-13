package com.cjmalloy.torrentfs.extension;

import java.util.ArrayList;
import java.util.List;


public interface Extension
{

    public static class ExtensionFactory
    {
        public static List<Extension> load(List<String> l)
        {
            List<Extension> ret = new ArrayList<Extension>();
            for (String ext : l)
            {
                switch (ext)
                {
                case "gpg":  ret.add(new GpgExtension()); break;
                case "html": ret.add(new HtmlExtension()); break;
                case "rev":  ret.add(new RevisionExtension()); break;
                }
            }
            return ret;
        }
    }
}
