package com.cjmalloy.torrentfs;

import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cjmalloy.torrentfs.model.Meta;
import com.turn.ttorrent.client.Client;
import com.turn.ttorrent.client.Client.ClientState;
import com.turn.ttorrent.client.SharedTorrent;
import com.turn.ttorrent.common.Torrent;


public class TfsClient
{
    private static final Logger logger = LoggerFactory.getLogger(TfsClient.class);

    private String rootPath = null;
    private Inet4Address address;
    private Map<String, TfsTorrent> lookup = new HashMap<String, TfsTorrent>();

    public TfsClient(Inet4Address address, String rootPath)
    {
        this.address = address;
        this.rootPath = rootPath;
    }

    public TfsTorrent addTorrent(Torrent torrent) throws IOException
    {
        String infoHash = torrent.getHexInfoHash();
        if (lookup.containsKey(infoHash)) return lookup.get(infoHash);

        File f = new File(rootPath + infoHash + "/");
        f.mkdirs();
        TfsTorrent tfs = new TfsTorrent(new Client(address, new SharedTorrent(torrent, f)), infoHash);
        tfs.client.addObserver(new Observer()
        {
            @Override
            public void update(Observable arg0, Object arg1)
            {
                onClientUpdate((Client) arg0, (ClientState) arg1);
            }
        });
        lookup.put(infoHash, tfs);
        tfs.client.share();
        return tfs;
    }

    public List<TfsTorrent> addTorrents(List<Torrent> l) throws IOException
    {
        if (l == null) return null;

        List<TfsTorrent> ret = new ArrayList<TfsTorrent>();
        for (Torrent t : l)
        {
            ret.add(addTorrent(t));
        }
        return ret;
    }

    protected void onClientUpdate(Client c, ClientState state)
    {
        logger.debug("torrent " + state.toString());
        switch (state)
        {
        case DONE:
        case SEEDING:
            onTorrentDone(c);
            break;
        case ERROR:
            break;
        case SHARING:
            break;
        case VALIDATING:
            break;
        case WAITING:
            break;
        }
    }

    private void onTorrentDone(Client c)
    {
        String infoHash = c.getTorrent().getHexInfoHash();
        if (lookup.get(infoHash).tfsData == null)
        {
            try
            {
                tryToAddTfs(infoHash);
            }
            catch (IOException e)
            {
                logger.warn("error trying to parse tfs file: {}", e.getMessage());
            }
        }
    }

    private void tryToAddTfs(String infoHash) throws IOException
    {
        File metaFile = new File(rootPath + infoHash + "/.tfs");
        if (!metaFile.exists()) return;

        Meta meta = Meta.load(metaFile);
        TfsTorrent t = lookup.get(infoHash);
        t.tfsData = meta;
        t.nested = addTorrents(meta.getNestedTorrents());

        if (t.nested == null) return;

        //TODO: symlink nested now?
    }
}
