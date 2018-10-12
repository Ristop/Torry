package ut.ee.xtorrent.client;

import be.christophedetroyer.torrent.TorrentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

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

        try (Stream<Path> paths = Files.walk(Paths.get("common/src/main/resources/test_torrent_files"))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(f -> {
                        try {
                            System.out.println(TorrentParser.parseTorrent(f.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
            ;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
