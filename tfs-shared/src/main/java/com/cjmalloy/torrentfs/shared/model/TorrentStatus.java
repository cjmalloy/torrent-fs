package com.cjmalloy.torrentfs.shared.model;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TorrentStatus {
  public boolean ready;
  public boolean[] nestedReady;
}
