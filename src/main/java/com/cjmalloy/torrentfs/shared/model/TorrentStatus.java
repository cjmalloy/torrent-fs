package com.cjmalloy.torrentfs.shared.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.cjmalloy.torrentfs.TfsTorrent;

@XmlRootElement
public class TorrentStatus
{
    public boolean ready;
    public boolean[] nestedReady;

    public TorrentStatus() {}

    public TorrentStatus(TfsTorrent t)
    {
        ready = t.isReady();
        if (t.nested != null)
        {
           nestedReady = new boolean[t.nested.size()];
            for (int i=0; i<t.nested.size(); i++)
            {
                nestedReady[i] = t.nested.get(i).isReady();
            }
        }
    }
}
