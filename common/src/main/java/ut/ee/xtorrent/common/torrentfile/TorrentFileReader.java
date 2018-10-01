package ut.ee.xtorrent.common.torrentfile;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TorrentFileReader implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(TorrentFileReader.class);

    private final Stream<Path> paths;

    public TorrentFileReader(String path) throws IOException {
        paths = Files.walk(Paths.get(path));
    }

    public Set<TorrentFile> readTorrentFiles() {
        return paths
                .filter(Files::isRegularFile)
                .filter(p -> isTorrentFile(p.getFileName().toString()))
                .map(this::readTorrentFile)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());

    }

    private Optional<TorrentFile> readTorrentFile(Path path) {
        try {
            TorrentFile torrentFile = new TorrentFile(path);
            return Optional.of(torrentFile);
        } catch (IOException e) {
            log.warn("Unable to parse torrent file from {}.", path);
            return Optional.empty();
        }
    }

    private boolean isTorrentFile(String fileName) {
        return FilenameUtils.getExtension(fileName).equals(".torrent");
    }

    @Override
    public void close() {
        paths.close();
    }

}
