#include <iostream>
#include <fstream>
#include <iterator>
#include <iomanip>

#define BOOST_ASIO_SEPARATE_COMPILATION
#define TORRENT_NO_DEPRECATE

#include "libtorrent/entry.hpp"
#include "libtorrent/bencode.hpp"
#include "libtorrent/torrent_info.hpp"
#include "libtorrent/lazy_entry.hpp"
#include <boost/filesystem/operations.hpp>


int main(int argc, char* argv[])
{
    using namespace libtorrent;
    using namespace boost::filesystem;

    if (argc != 2)
    {
        std::cerr << "usage: dump_torrent torrent-file\n";
        return 1;
    }

    try
    {
        int size = file_size(argv[1]);
        if (size > 10 * 1000000)
        {
            std::cerr << "file too big (" << size << "), aborting\n";
            return 1;
        }
        std::vector<char> buf(size);
        std::ifstream(argv[1], std::ios_base::binary).read(&buf[0], size);
        lazy_entry e;
        error_code ec;
        int ret = lazy_bdecode(&buf[0], &buf[0] + buf.size(), e, ec);

        if (ret != 0)
        {
            std::cerr << "invalid bencoding: " << ret << std::endl;
            return 1;
        }

        std::cout << "\n\n----- raw info -----\n\n";
        std::cout << e.string_value() << std::endl;

        torrent_info t(e);

        // print info about torrent
        std::cout << "\n\n----- torrent file info -----\n\n";
        std::cout << "nodes:\n";
        typedef std::vector<std::pair<std::string, int> > node_vec;
        node_vec const& nodes = t.nodes();
        for (node_vec::const_iterator i = nodes.begin(), end(nodes.end());
            i != end; ++i)
        {
            std::cout << i->first << ":" << i->second << "\n";
        }
        std::cout << "trackers:\n";
        for (std::vector<announce_entry>::const_iterator i = t.trackers().begin();
            i != t.trackers().end(); ++i)
        {
            std::cout << i->tier << ": " << i->url << "\n";
        }

        std::cout << "number of pieces: " << t.num_pieces() << "\n";
        std::cout << "piece length: " << t.piece_length() << "\n";
        std::cout << "info hash: " << t.info_hash() << "\n";
        std::cout << "comment: " << t.comment() << "\n";
        std::cout << "created by: " << t.creator() << "\n";
        std::cout << "files:\n";
        for (int i=0; i<t.num_files(); i++)
        {
        	file_entry f = t.files().at(i);
            int first = t.map_file(i, 0, 1).piece;
            int last = t.map_file(i, f.size - 1, 1).piece;
            std::cout << "  " << std::setw(11) << t.num_files()
                << " " << f.path << "[ " << first << ", "
                << last << " ]\n";
        }
    }
    catch (std::exception& e)
    {
        std::cout << e.what() << "\n";
    }

    return 0;
}
