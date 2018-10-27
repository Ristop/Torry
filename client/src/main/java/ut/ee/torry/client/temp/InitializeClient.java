package ut.ee.torry.client.temp;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class InitializeClient {

    private static final Logger log = LoggerFactory.getLogger(InitializeClient.class);

    // It's initialized from TestBean.java class. Couldn't find any better solutions.
    // Possible to pass all the config parameters from there
    // The path here is location where are client torrent files located
    // (client is downloading and uploading those torrents).
    // The client is uploading those files in case where the torrent files are already
    // perfectly in this folder, otherwise it will start to download them to that folder
    InitializeClient(String clientFolderPath) {
        log.info("Client Initialized.");
        List<Torrent> torrentFiles = readTorrentFiles(clientFolderPath);
        for (Torrent torrentFile : torrentFiles) {
            new TorrentHandler(torrentFile, clientFolderPath);
        }
    }

    private ArrayList<Torrent> readTorrentFiles(String location) {
        ArrayList<Torrent> torrentFiles = new ArrayList<>();

        try (Stream<Path> paths = Files.walk(Paths.get(location))) {
            paths.filter(Files::isRegularFile)
                    .forEach(f -> {
                        try {
                            if (f.toString().endsWith(".torrent")) {
                                torrentFiles.add(TorrentParser.parseTorrent(f.toString()));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return torrentFiles;
    }


}
