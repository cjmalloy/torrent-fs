torrent-fs
==========

[![Build Status](https://travis-ci.org/cjmalloy/torrent-fs.svg?branch=master)](https://travis-ci.org/cjmalloy/torrent-fs)

Client for a torrent-based file system.

The goal of torrent-fs (tfs) is to extend the existing bittorent spec to create a distributed filesystem. This spec is backwards compatible, so all existing torrents are also tfs torrents. The main features added are

* Metadata
* Nested torrents
* GPG signing
* Semantic extensibility
* Dual bencode/json file format support

A tfs client operates by presenting a tfs torrent and an extension. The refererence client supports the html extension, and acts as a type of web browser.

All tfs torrents have a .tfs JSON file. If the torrent is a single file torrent ("length" torrent), then the entire payload is that file. If the torrent is a folder torrent ("files" torrent), then the .tfs file is in the root directory.

The .tfs file contains the nested torrents, GPG signatures, and a list of extenstions. Here is a sample that supports the html extension:

```javascript
{
  author: "chris",
  origin: "openbitpub.com",
  adult: false,
  solicitation: false,
  nested: [
    {encoding: "bencode", mount: "./media/movie.webm", torrent: "base 64 bencode"},
    {encoding: "magnet",  mount: "./images/",          torrent: "magent uri"},
    {encoding: "json",    mount: "./pdf/",             torrent: {/* json */}}
  ],
  extensions: ["html", "revision", "gpg"],
  "html": {
    version: 1.0,
    index: "./index.html",
    ajax: true,
    ajax_whitelist: ["openbitpub.com"]
  },
  "revision": {
    version: 1.0,
    prev_revision: "info_hash of previous tfs torrent",
    revision_notes: "some comments here"
  },
  "gpg": {
    version: 1.0,
    nested: [
      /* One sig object for each nested torrent */
      {signers: ["AE96 2A02 ..."], sig: ["detached sig of info_hash for nested #1"]},
      {signers: ["AE96 2A02 ..."], sig: ["detached sig of info_hash for nested #2"]},
      {signers: ["AE96 2A02 ..."], sig: ["detached sig of info_hash for nested #3"]}
    ]
  }
}
```

See Also
========

Tfs Browser: A module to browse html content.  
https://github.com/cjmalloy/tfs-browser

Tfs GUI: A gui to manage torrent file systems.  
https://github.com/cjmalloy/tfs-gui
