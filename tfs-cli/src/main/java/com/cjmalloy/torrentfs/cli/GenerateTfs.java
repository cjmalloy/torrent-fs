package com.cjmalloy.torrentfs.cli;

import jargs.gnu.CmdLineParser;

import java.io.File;
import java.io.PrintStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cjmalloy.torrentfs.model.Encoding;
import com.cjmalloy.torrentfs.util.TfsUtil;
import com.turn.ttorrent.cli.TorrentMain;
import com.turn.ttorrent.common.Torrent;

public class GenerateTfs
{

    private static final Logger logger = LoggerFactory.getLogger(TorrentMain.class);

    /**
     * Display program usage on the given {@link PrintStream}.
     */
    private static void usage(PrintStream s)
    {
        usage(s, null);
    }

    /**
     * Display a message and program usage on the given {@link PrintStream}.
     */
    private static void usage(PrintStream s, String msg)
    {
        if (msg != null)
        {
            s.println(msg);
            s.println();
        }

        s.println("usage: Torrent [options] [file|directory]");
        s.println();
        s.println("Available options:");
        s.println("  -h,--help             Show this help and exit.");
        s.println();
        s.println("  -a,--announce         Tracker URL (can be repeated).");
        s.println();
    }

    /**
     * Torrent reader and creator.
     *
     * <p>
     * You can use the {@code main()} function of this class to read or create
     * torrent files. See usage for details.
     * </p>
     *
     */
    public static void main(String[] args)
    {
        BasicConfigurator.configure(new ConsoleAppender(new PatternLayout("%-5p: %m%n")));

        CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option help = parser.addBooleanOption('h', "help");
        CmdLineParser.Option announce = parser.addStringOption('a', "announce");

        try
        {
            parser.parse(args);
        }
        catch (CmdLineParser.OptionException oe)
        {
            System.err.println(oe.getMessage());
            usage(System.err);
            System.exit(1);
        }

        // Display help and exit if requested
        if (Boolean.TRUE.equals((Boolean) parser.getOptionValue(help)))
        {
            usage(System.out);
            System.exit(0);
        }

        // For repeated announce urls
        @SuppressWarnings("unchecked")
        Vector<String> announceURLs = (Vector<String>) parser.getOptionValues(announce);

        String[] otherArgs = parser.getRemainingArgs();

        try
        {
            // Process the announce URLs into URIs
            List<URI> announceURIs = new ArrayList<URI>();
            for (String url : announceURLs)
            {
                announceURIs.add(new URI(url));
            }

            // Create the announce-list as a list of lists of URIs
            // Assume all the URI's are first tier trackers
            List<List<URI>> announceList = new ArrayList<List<URI>>();
            announceList.add(announceURIs);

            File source = new File(otherArgs[0]);
            if (!source.exists() || !source.canRead()) { throw new IllegalArgumentException("Cannot access source file or directory "
                    + source.getName()); }

            String creator = String.format("%s (ttorrent)", System.getProperty("user.name"));

            List<Torrent> torrents = TfsUtil.generateTorrentFromTfs(source, Encoding.BENCODE_BASE64, announceList, creator);
            TfsUtil.saveTorrents(new File("."), torrents);
        }
        catch (Exception e)
        {
            logger.error("{}", e.getMessage(), e);
            System.exit(2);
        }
    }
}
