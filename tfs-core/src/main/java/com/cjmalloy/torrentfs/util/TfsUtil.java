package com.cjmalloy.torrentfs.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.cjmalloy.torrentfs.model.Meta;
import com.cjmalloy.torrentfs.model.Nested;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.turn.ttorrent.common.Torrent;

public class TfsUtil
{
    public static Torrent generateLegacyTorrent(File source, final List<String> ignoreFilter, List<List<URI>> announceList, String createdBy)
            throws InterruptedException, IOException
    {
        if (!source.isDirectory())
        {
            return Torrent.create(source, announceList, createdBy);
        }

        IOFileFilter fileFilter = TrueFileFilter.TRUE;
        if (ignoreFilter != null)
        {
            fileFilter = new IOFileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    String s = file.toString();
                    for (String prefix : ignoreFilter)
                    {
                        if (s.startsWith(prefix)) return false;
                    }
                    return true;
                }

                @Override
                public boolean accept(File dir, String name)
                {
                    return accept(Paths.get(dir.toString(), name).toFile());
                }
            };
        }
        List<File> includeFiles = new ArrayList<>(FileUtils.listFiles(source, fileFilter, TrueFileFilter.TRUE));
        return Torrent.create(source, includeFiles, announceList, createdBy);
    }

    public static List<Torrent> generateTorrentFromTfs(File source, Encoding encoding, List<List<URI>> announceList, String createdBy)
            throws InterruptedException, IOException
    {
        Meta tfs = null;
        File rootTfs = null;
        boolean isDir = source.isDirectory();
        if (!isDir && source.toString().endsWith(".tfs"))
        {
            rootTfs = source;
        }
        else if (isDir)
        {
            rootTfs = Paths.get(source.toString(), ".tfs").toFile();
        }
        tfs = Meta.load(rootTfs);
        if (tfs == null || tfs.nested == null || tfs.nested.size() == 0)
        {
            // No .tfs file found, or this tfs torrent has no nested torrents
            // We can just use the legacy generator
            return Arrays.asList(generateLegacyTorrent(source, null, announceList, createdBy));
        }

        List<Torrent> ret = new ArrayList<>();
        List<String> ignoreFilter = new ArrayList<>();
        for (Nested n : tfs.nested)
        {
            File nestedSource = Paths.get(source.toString(), n.mount).toFile();
            ignoreFilter.add(nestedSource.getCanonicalPath());
            List<Torrent> ts = generateTorrentFromTfs(nestedSource, encoding, announceList, createdBy);
            ret.addAll(ts);

            // The last torrent corresponds to nested torrent we are currently iterating
            Torrent t = ts.get(ts.size()-1);
            n.encoding = encoding;
            n.torrent = getJsonFromTorrent(encoding, t);
        }
        FileOutputStream fos = new FileOutputStream(rootTfs);
        fos.write(JsonUtil.prettyPrint(tfs.writeJson()).getBytes(Charset.forName("UTF-8")));
        fos.close();
        ret.add(generateLegacyTorrent(source, ignoreFilter, announceList, createdBy));
        return ret;
    }

    public static JsonElement getJsonFromTorrent(Encoding encoding, Torrent torrent) throws UnsupportedEncodingException
    {
        switch (encoding)
        {
        case BENCODE_BASE64:
            return new JsonPrimitive(Base64.encodeBase64String(torrent.getEncoded()));
        case JSON:
            return null; //TODO
        case MAGNET:
            return new JsonPrimitive(getMagnetAddress(torrent));
        }
        return null;
    }

    public static String getMagnetAddress(Torrent torrent) throws UnsupportedEncodingException
    {
        String magnet = "magnet:?xt=urn:btih:" + torrent.getHexInfoHash()
                      + "&dn=" + URLEncoder.encode(torrent.getName(), "UTF-8");
        for (List<URI> uris : torrent.getAnnounceList())
        for (URI uri : uris)
        {
            magnet += "&tr=" + URLEncoder.encode(uri.toString(), "UTF-8");
        }
        return magnet;
    }

    public static Torrent getTorrentFromBencode(String torrent_base64) throws IOException
    {
        return new Torrent(Base64.decodeBase64(torrent_base64.getBytes(Charset.forName("UTF-8"))), false);
    }

    public static Torrent getTorrentFromJson(Encoding encoding, JsonElement torrent) throws IOException
    {
        switch (encoding)
        {
        case BENCODE_BASE64:
            return getTorrentFromBencode(torrent.getAsString());
        case JSON:
            return null; //TODO
        case MAGNET:
            return null;
        }
        return null;
    }

    public static void saveTorrents(File dir, List<Torrent> torrents) throws IOException
    {
        FileOutputStream fos = null;
        for (Torrent t : torrents)
        {
            try
            {
                fos = new FileOutputStream(Paths.get(dir.toString(), t.getHexInfoHash() + ".torrent").toFile());
                t.save(fos);
                IOUtils.closeQuietly(fos);
                fos = null;
            }
            finally
            {
                if (fos != null) IOUtils.closeQuietly(fos);
            }
        }
    }

    public enum Encoding
    {
        BENCODE_BASE64("bencode"),
        MAGNET("magnet"),
        JSON("json");

        private String strValue;

        Encoding(String strValue)
        {
            this.strValue = strValue;
        }

        @Override
        public String toString()
        {
            return strValue;
        }

        public static Encoding getEncoding(String strValue)
        {
            for (Encoding enc : Encoding.values())
            {
                if (enc.toString().equalsIgnoreCase(strValue)) return enc;
            }
            return null;
        }
    }
}
