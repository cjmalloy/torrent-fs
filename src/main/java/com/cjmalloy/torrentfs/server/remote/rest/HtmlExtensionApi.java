package com.cjmalloy.torrentfs.server.remote.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;

import com.cjmalloy.torrentfs.extension.HtmlExtension;
import com.cjmalloy.torrentfs.server.TfsClientSingleton;

@Path("/ext/html/")
public class HtmlExtensionApi
{
    @GET @Path("/index/{info_hash}")
    @Produces(MediaType.TEXT_PLAIN)
    public String torrentStatus(@PathParam("info_hash") String infoHash)
    {
        HtmlExtension ext = (HtmlExtension) TfsClientSingleton.get().getTorrent(infoHash).tfsData.extensions.get("html");
        if (ext == null) throw new WebApplicationException(400);

        return ext.index;
    }
}
