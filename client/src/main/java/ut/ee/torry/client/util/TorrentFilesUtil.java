package ut.ee.torry.client.util;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.ee.torry.client.ClientStarter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * Helper class with anything connected to torrent files
 */
public final class TorrentFilesUtil {

    private static final Logger log = LoggerFactory.getLogger(ClientStarter.class);

    public static final String TORRENT_FILE_EXTENSION = ".torrent";

    private TorrentFilesUtil() {
    }

    /**
     * @param torrentFilesDir Directory from where .torrent files are searched
     * @return list of all torrent files found (non-torrent files are ignored).
     * @throws IOException
     */
    public static List<Torrent> readAllTorrentFiles(String torrentFilesDir) throws IOException {
        Path torrentFilesDirPath = Paths.get(torrentFilesDir);

        if (!Files.isReadable(torrentFilesDirPath)) {
            log.error("{} is not readable", torrentFilesDir);
            throw new IOException(torrentFilesDir + " is not a readable.");
        }

        if (!Files.isDirectory(torrentFilesDirPath)) {
            log.error("{} is not a directory", torrentFilesDir);
            throw new IOException(torrentFilesDir + " is not a directory.");
        }

        log.info("Loading torrent files from directory: {}.", torrentFilesDir);

        List<Torrent> torrents = Files.walk(torrentFilesDirPath)
                .filter(s -> s.toString().endsWith(TORRENT_FILE_EXTENSION))
                .map(TorrentFilesUtil::tryReadTorrentFile)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());

        log.info("Read following {} torrent files: {}",
                torrents.size(),
                torrents.stream()
                        .map(Torrent::getName)
                        .collect(toList())
        );

        return torrents;
    }

    /**
     * @param torrentFilePath path of the torrent file
     * @return Optional if the torrent file which is empty if the parsing fails.
     */
    private static Optional<Torrent> tryReadTorrentFile(Path torrentFilePath) {
        try {
            return Optional.of(TorrentParser.parseTorrent(torrentFilePath.toString()));
        } catch (IOException e) {
            log.warn("Unable to read file: {}, cause: {}", torrentFilePath, e.getCause());
            return Optional.empty();
        }
    }

}
