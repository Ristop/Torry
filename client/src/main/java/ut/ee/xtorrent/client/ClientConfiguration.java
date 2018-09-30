package ut.ee.xtorrent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import ut.ee.xtorrent.common.torrentfile.TorrentFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class ClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ClientConfiguration.class);

    // Client application logic starts from here
    public ClientConfiguration() {
        log.info("Client Initialized.");
        torretFilesTest();
    }

    private void torretFilesTest() {
        log.info("Torrent files testing started");
        for (TorrentFile torrent : getFolderTorrentFiles("common/src/main/resources/test_torrent_files")) {
            log.info(torrent.toString());
        }
    }

    private List<TorrentFile> getFolderTorrentFiles(String folderPath) {
        List<TorrentFile> torrentFiles = new ArrayList<>();
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            try {
                for (File file : listOfFiles) {
                    if (isTorrentFile(file.getName())) {
                        String fileLocation = folderPath + "/" + file.getName();
                        torrentFiles.add(new TorrentFile(fileLocation));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return torrentFiles;
    }

    private boolean isTorrentFile(String fileName) {
        return fileName.endsWith(".torrent");
    }

}
