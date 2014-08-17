torrent-fs
==========

Specification for a torrent-based file system.

The goal of torrent-fs (tfs) is to extend the existing bittorent spec to create a distributed filesystem. This spec is backwards compatible, so all existing torrents are also tfs torrents. The main features added are

* Metadata
* Nested torrents
* GPG signing
* Semantic extensibility
* Dual bencode/json file format support

A tfs client operates by presenting a tfs torrent and an extension. The refererence client supports the html extension, and acts as a type of web browser.

All tfs torrents have a .metadata JSON file. If the torrent is a single file torrent ("length" torrent), then the entire payload is that file. If the torrent is a folder torrent ("files" torrent), then the .metadata file is in the root directory.

The .metadata file contains the netsted torrents, GPG signatures, and a list of extenstions. Here is a sample that supports the html extension:

```javascript
{
  hash: "hash of this torrent (with gpg extension blanked)",
  author: "chris",
  origin: "openbitpub.com",
  adult: false,
  solicitation: false,
  nested: [
    {encoding: "bencode", mount: "./media/bigMovie.webm", torrent: "bencode encoded torrent"},
    {encoding: "json",    mount: "./images/",             torrent: "json encoded torrent"}
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
    signers: ["AE96 2A02 D29B FE4A 4BB2  805F DE40 1E0D 5873 000A"],
    sig: ["detached signature of the hash given above"]
  }
}
```
