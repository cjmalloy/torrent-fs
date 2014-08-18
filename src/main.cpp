#include <iostream>
#include <fstream>
#include <iterator>
#include <iomanip>

#define BOOST_ASIO_SEPARATE_COMPILATION

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
#if BOOST_VERSION < 103400
        boost::filesystem::path::default_name_check(boost::filesystem::no_check);
#endif

#ifndef BOOST_NO_EXCEPTIONS
        try
        {
#endif

                int size = file_size(argv[1]);
                if (size > 10 * 1000000)
                {
                        std::cerr << "file too big (" << size << "), aborting\n";
                        return 1;
                }
                std::vector<char> buf(size);
                std::ifstream(argv[1], std::ios_base::binary).read(&buf[0], size);
                lazy_entry e;
                int ret = lazy_bdecode(&buf[0], &buf[0] + buf.size(), e);

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
                int index = 0;
                for (torrent_info::file_iterator i = t.begin_files();
                        i != t.end_files(); ++i, ++index)
                {
                        int first = t.map_file(index, 0, 1).piece;
                        int last = t.map_file(index, i->size - 1, 1).piece;
                        std::cout << "  " << std::setw(11) << i->size
                                << " " << t.files().at(i->path_index).path << "[ " << first << ", "
                                << last << " ]\n";
                }

#ifndef BOOST_NO_EXCEPTIONS
        }
        catch (std::exception& e)
        {
                std::cout << e.what() << "\n";
        }
#endif

        return 0;
}
