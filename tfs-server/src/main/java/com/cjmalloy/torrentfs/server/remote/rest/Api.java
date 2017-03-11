package com.cjmalloy.torrentfs.server.remote.rest;

import com.cjmalloy.torrentfs.TfsTorrent;
import com.cjmalloy.torrentfs.server.TfsClientSingleton;
import com.cjmalloy.torrentfs.server.util.ModelUtil;
import com.cjmalloy.torrentfs.shared.model.TorrentStatus;
import com.cjmalloy.torrentfs.util.BencodeUtil;
import com.turn.ttorrent.bcodec.BDecoder;
import com.turn.ttorrent.common.Torrent;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

@Path("/")
public class Api {
  private static final Logger logger = LoggerFactory.getLogger(Api.class);

  @POST
  @Path("/add")
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @Produces(MediaType.TEXT_PLAIN)
  public String addTorrent(InputStream bencode) {
    try {
      return TfsClientSingleton.get().addTorrent(new Torrent(IOUtils.toByteArray(bencode), false)).infoHash;
    } catch (IOException e) {
      e.printStackTrace();
      throw new WebApplicationException(400);
    } catch (NoSuchAlgorithmException e) {
      logger.error("Required Crypto algorithms not installed.");
      throw new WebApplicationException(500);
    }
  }

  @POST
  @Path("/print")
  @Consumes(MediaType.APPLICATION_OCTET_STREAM)
  @Produces(MediaType.APPLICATION_JSON)
  public String printTorrent(InputStream bencode) {
    String s;
    try {
      s = BencodeUtil.printValue(BDecoder.bdecode(bencode));
    } catch (IOException e) {
      e.printStackTrace();
      throw new WebApplicationException(400);
    }
    return s;
  }

  @GET
  @Path("/status/{info_hash}")
  @Produces(MediaType.APPLICATION_JSON)
  public TorrentStatus torrentStatus(@PathParam("info_hash") String infoHash) {
    TfsTorrent t = TfsClientSingleton.get().getTorrent(infoHash);
    if (t == null) new WebApplicationException(404);

    return ModelUtil.getTorrentStatus(t);
  }
}
