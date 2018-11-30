package ut.ee.torry.client.util;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TorrentFilesUtilTest {

    @Test
    void readAllTorrentFiles() throws IOException {
        String torrent1_path = "./src/test/java/ut/ee/torry/client/util/test_torrents_dir/file1.txt.torrent";
        String torrent2_path = "./src/test/java/ut/ee/torry/client/util/test_torrents_dir/file2.txt.torrent";

        Torrent torrent1 = TorrentParser.parseTorrent(torrent1_path);
        Torrent torrent2 = TorrentParser.parseTorrent(torrent2_path);

        List<Torrent> torrents = new ArrayList<>();
        torrents.add(torrent1);
        torrents.add(torrent2);

        List<Torrent> dirToTest = TorrentFilesUtil.readAllTorrentFiles(
                "./src/test/java/ut/ee/torry/client/util/test_torrents_dir"
        );

        assertEquals(dirToTest.size(), torrents.size());

        dirToTest.sort(Comparator.comparing(Torrent::getName));

        assertEquals(dirToTest.get(0).getName(), torrents.get(0).getName());
        assertEquals(dirToTest.get(0).getInfo_hash(), torrents.get(0).getInfo_hash());
        assertEquals(dirToTest.get(0).getPieces(), torrents.get(0).getPieces());

        assertEquals(dirToTest.get(1).getName(), torrents.get(1).getName());
        assertEquals(dirToTest.get(1).getInfo_hash(), torrents.get(1).getInfo_hash());
        assertEquals(dirToTest.get(1).getPieces(), torrents.get(1).getPieces());
    }


}
