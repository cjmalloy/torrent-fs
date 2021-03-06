package com.cjmalloy.torrentfs.model;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import com.cjmalloy.torrentfs.util.JsonUtil.Factory;
import com.cjmalloy.torrentfs.util.JsonUtil.HasJson;
import com.cjmalloy.torrentfs.util.TfsUtil;
import com.google.gson.*;
import com.turn.ttorrent.common.Torrent;


public class Nested implements HasJson {
  public static final Factory<Nested> FACTORY = new Factory<Nested>() {
    @Override
    public Nested create() {
      return new Nested();
    }
  };

  /**
   * Locks this nested torrent to prevent it from being regenerated if some of
   * the files change. Use this if you are referencing a pre-existing torrent
   * and you do not want to accidently regenerate it.
   */
  public boolean readOnly = false;

  /**
   * Determines the encoding type of the "torrent" field.
   *
   * <p>Currently only "bencode" is implemented.</p>
   */
  public Encoding encoding;

  /**
   * The mount point for this torrent. The mount point must be an empty folder
   * or file in the parent tfs. If the mount point is located within a folder
   * generated by another nested torrent there will be an error.
   */
  public String mount;

  /**
   * The raw torrent data. The encoding type is set in the "encoding" field.
   */
  public JsonElement torrent;

  /**
   * The mound point when resolved against the parents absolute path.
   */
  public transient File absolutePath = null;

  /**
   * The metadata for this torrent, if it is a tfs torrent. Otherwise, this
   * value should be null.
   */
  public transient Meta meta = null;

  public Torrent getTorrent() throws IOException, NoSuchAlgorithmException {
    return TfsUtil.getTorrentFromJson(encoding, torrent);
  }

  @Override
  public Nested parseJson(JsonObject o) {
    if (o.has("readOnly")) {
      readOnly = o.get("readOnly").getAsBoolean();
    }
    if (o.has("encoding")) {
      encoding = Encoding.getEncoding(o.get("encoding").getAsString());
    }
    if (o.has("mount")) {
      mount = o.get("mount").getAsString();
    }
    if (o.has("torrent")) {
      torrent = o.get("torrent");
    }
    return this;
  }

  @Override
  public JsonElement writeJson() {
    JsonObject o = new JsonObject();
    o.add("readOnly", new JsonPrimitive(readOnly));
    o.add("encoding", new JsonPrimitive(encoding.toString()));
    o.add("mount", new JsonPrimitive(mount));
    o.add("torrent", torrent);
    return o;
  }
}
