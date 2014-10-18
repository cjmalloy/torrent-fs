package com.cjmalloy.torrentfs.server.remote.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;

import com.cjmalloy.torrentfs.TfsTorrent;
import com.cjmalloy.torrentfs.server.TfsClientSingleton;
import com.cjmalloy.torrentfs.server.util.ModelUtil;
import com.cjmalloy.torrentfs.shared.model.TorrentStatus;
import com.cjmalloy.torrentfs.util.TfsUtil;
import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.common.Torrent;

@Path("/")
public class Api
{

    @POST @Path("/add")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.TEXT_PLAIN)
    public String addTorrent(InputStream bencode)
    {
        try
        {
            return TfsClientSingleton.get().addTorrent(new Torrent(IOUtils.toByteArray(bencode), false)).infoHash;
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new WebApplicationException(400);
        }
    }

    @POST @Path("/print")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String printTorrent(InputStream bencode)
    {
        String s;
        try
        {
            s = TfsUtil.printValue(BDecoder.bdecode(bencode));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new WebApplicationException(400);
        }
        return s;
    }

    @GET @Path("/status/{info_hash}")
    @Produces(MediaType.APPLICATION_JSON)
    public TorrentStatus torrentStatus(@PathParam("info_hash") String infoHash)
    {
        TfsTorrent t = TfsClientSingleton.get().getTorrent(infoHash);
        if (t == null) return null;

        return ModelUtil.getTorrentStatus(t);
    }
}
