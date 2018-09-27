package ut.ee.xtorrent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
        for (TorrentFIle torrent: getFolderTorrentFiles("client/src/main/resources/torrent_files")) {
            log.info(torrent.toString());
        }
    }

    private ArrayList<TorrentFIle> getFolderTorrentFiles(String folderPath){               //gives only torrent files (not others)
        ArrayList<TorrentFIle> torrentFiles = new ArrayList<>();
        File folder = new File(folderPath);
        File[] listOfFiles = folder.listFiles();

        if (listOfFiles != null) {
            try {
                for (File file : listOfFiles) {
                    if (isTorrentFile(file.getName())) {
                        String fileLocation = folderPath + "/" + file.getName();
                        torrentFiles.add(new TorrentFIle(fileLocation));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return torrentFiles;
    }

    private boolean isTorrentFile(String filename) {
        return filename.split("\\.")[1].equals("torrent");
    }

}
