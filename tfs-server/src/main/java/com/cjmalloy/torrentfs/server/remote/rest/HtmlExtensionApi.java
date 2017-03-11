package com.cjmalloy.torrentfs.server.remote.rest;

import com.cjmalloy.torrentfs.extension.HtmlExtension;
import com.cjmalloy.torrentfs.server.TfsClientSingleton;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

@Path("/ext/html/")
public class HtmlExtensionApi {
  @GET
  @Path("/index/{info_hash}")
  public Response getIndex(@PathParam("info_hash") String infoHash) {
    HtmlExtension ext = (HtmlExtension) TfsClientSingleton.get().getTorrent(infoHash).tfsData.extensions.get("html");
    if (ext == null) throw new WebApplicationException(400);

    URI uri = UriBuilder.fromUri("/ext/html/static/" + infoHash + "/" + ext.index).build();
    return Response.seeOther(uri).build();
  }
}
