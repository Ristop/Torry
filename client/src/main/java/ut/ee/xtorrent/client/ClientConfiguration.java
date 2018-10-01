package ut.ee.xtorrent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import ut.ee.xtorrent.common.torrentfile.TorrentFile;
import ut.ee.xtorrent.common.torrentfile.TorrentFileReader;

import java.io.IOException;
import java.util.Set;

@Configuration
public class ClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ClientConfiguration.class);

    // Client application logic starts from here
    public ClientConfiguration() {
        log.info("Client Initialized.");
        readTorrentFiles();
    }

    private void readTorrentFiles() {
        log.info("Torrent files testing started");

        try (TorrentFileReader reader = new TorrentFileReader("common/src/main/resources/test_torrent_files")) {
            Set<TorrentFile> torrentFiles = reader.readTorrentFiles();
            torrentFiles.forEach(tf -> System.out.println("Read torrent file: " + tf));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
