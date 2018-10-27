package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ut.ee.torry.client.ClientConfiguration.DOWNLOADED_FILES_DIR;
import static ut.ee.torry.client.ClientConfiguration.PEER_ID;
import static ut.ee.torry.client.ClientConfiguration.PORT;
import static ut.ee.torry.client.ClientConfiguration.TORRENT_FILES_DIR;
import static ut.ee.torry.client.util.TorrentFilesUtil.readAllTorrentFiles;

@Component
public class ClientStarter {

    private static final Logger log = LoggerFactory.getLogger(ClientStarter.class);

    public ClientStarter(
            @Qualifier(PEER_ID) String peerId,
            @Qualifier(PORT) int port,
            @Qualifier(TORRENT_FILES_DIR) String torrentFilesDir,
            @Qualifier(DOWNLOADED_FILES_DIR) String downloadedFiledDir
    ) throws IOException {
        log.info("Starting client with peer id {}, running on port {}.", peerId, port);

        List<Torrent> torrents = readAllTorrentFiles(torrentFilesDir);
        log.info("Read following {} torrent files: {}",
                torrents.size(),
                torrents.stream()
                        .map(Torrent::getName)
                        .collect(toList())
        );

        log.info("Downloaded files directory: {}.", downloadedFiledDir);
    }

}
