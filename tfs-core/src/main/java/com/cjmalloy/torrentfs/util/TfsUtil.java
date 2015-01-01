package com.cjmalloy.torrentfs.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;

import com.cjmalloy.torrentfs.model.Encoding;
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
            return Torrent.create(source, Torrent.DEFAULT_PIECE_LENGTH, announceList, createdBy);
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
        return Torrent.create(source, includeFiles, Torrent.DEFAULT_PIECE_LENGTH, announceList, createdBy);
    }

    /**
     * Generate a list of torrent files for this tfs.
     *
     * @param source the root directory or file
     * @param encoding the encoding to use for nested torrents
     * @param announceList the announce list
     * @param createdBy creator signature
     * @param cache cache directory to initialize for seeding (null to skip)
     * @param link use symbolic links instead of copying to the cache directory
     * @return the list of torrent files
     */
    public static List<Torrent> generateTorrentFromTfs(File source, Encoding encoding, List<List<URI>> announceList, String createdBy, File cache, boolean link)
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
            Torrent t = generateLegacyTorrent(source, null, announceList, createdBy);
            initSeedCache(source, t.getHexInfoHash(), cache, link);
            return Arrays.asList(t);
        }

        List<Torrent> ret = new ArrayList<>();
        List<String> ignoreFilter = new ArrayList<>();
        for (Nested n : tfs.nested)
        {
            File nestedSource = Paths.get(source.toString(), n.mount).toFile();
            ignoreFilter.add(nestedSource.getCanonicalPath());
            if (n.readOnly) continue;

            List<Torrent> ts = generateTorrentFromTfs(nestedSource, encoding, announceList, createdBy, cache, link);
            for (Torrent t : ts)
            {
                initSeedCache(n.absolutePath, t.getHexInfoHash(), cache, link);
            }
            ret.addAll(ts);

            // The last torrent corresponds to nested torrent we are currently iterating
            Torrent t = ts.get(ts.size()-1);
            n.encoding = encoding;
            n.torrent = getJsonFromTorrent(encoding, t);
        }
        writeTfs(rootTfs, tfs);
        Torrent t = generateLegacyTorrent(source, ignoreFilter, announceList, createdBy);
        initSeedCache(source, t.getHexInfoHash(), cache, link);
        ret.add(t);
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

    public static void initSeedCache(File source, String infoHash, File cache, boolean link) throws IOException
    {
        if (cache == null) return;

        File seedDir = new File(cache, infoHash);
        if (seedDir.exists()) return;

        seedDir.mkdir();
        File target = new File(seedDir, source.getName());
        if (link)
        {
            Files.createSymbolicLink(target.toPath(), source.toPath());
        }
        else if (source.isDirectory())
        {
            FileUtils.copyDirectory(source, target);
        }
        else
        {
            FileUtils.copyFile(source, target);
        }
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

    public static void writeTfs(File f, Meta meta) throws IOException
    {
        FileOutputStream fos = null;
        try
        {
            fos = new FileOutputStream(f);
            fos.write(JsonUtil.prettyPrint(meta.writeJson()).getBytes(Charset.forName("UTF-8")));
        }
        finally
        {
            if (fos != null) IOUtils.closeQuietly(fos);
        }
    }
}
