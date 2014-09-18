package com.cjmalloy.torrentfs.server.remote.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.cjmalloy.torrentfs.util.TfsUtil;
import com.turn.ttorrent.bcodec.BDecoder;

@Path("/")
public class Api
{

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

    @POST @Path("/add")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public String addTorrent(InputStream bencode)
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
}
