package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;
import org.junit.jupiter.api.Test;
import ut.ee.torry.client.util.TorrentFilesUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ReadTorrentFileTest {

    @Test
    public void readTwoTorrentFilesUsingLibaryTest() throws IOException {
        List<String> fileNames = new ArrayList<>();
        String torrentFile1Name = "gangnam.torrent";
        String torrentFile2Name = "shrek.torrent";
        fileNames.add(torrentFile1Name);
        fileNames.add(torrentFile2Name);

        List<Torrent> parsedTorrentFiles = new ArrayList<>();

        for (String fileName : fileNames) {
            parsedTorrentFiles.add(TorrentParser.parseTorrent("src/test/resources/test_torrent_files/" + fileName));
        }

        assertEquals(2, parsedTorrentFiles.size());
    }

    @Test
    public void readTorrentFilesFromDirectoryTest() throws IOException {
        List<Torrent> torrents = TorrentFilesUtil
                .readAllTorrentFiles("src/test/resources/test_torrent_files/");

        assertEquals(2, torrents.size());
    }

}
