package com.cjmalloy.torrentfs.server.util;

import com.cjmalloy.torrentfs.TfsTorrent;
import com.cjmalloy.torrentfs.shared.model.TorrentStatus;


public class ModelUtil
{

    public static TorrentStatus getTorrentStatus(TfsTorrent t)
    {
        TorrentStatus ret = new TorrentStatus();
        ret.ready = t.isReady();
        if (t.nested != null)
        {
           ret.nestedReady = new boolean[t.nested.size()];
            for (int i=0; i<t.nested.size(); i++)
            {
                ret.nestedReady[i] = t.nested.get(i).isReady();
            }
        }
        return ret;
    }
}
