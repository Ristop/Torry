package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentParser;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PieceHandlerTest {

    private String seederDownloadFolder;
    private String downloaderDownloadFolder;
    private String singleFileTorLoc;
    private String multiFile1TorLoc;
    private String multiFile2TorLoc;

    @Before
    public void SetDownloadFolders() {
        this.downloaderDownloadFolder = "src/test/resources/pieces_test_files/downloader/downloads";
        this.seederDownloadFolder = "src/test/resources/pieces_test_files/seeder/downloads";
        this.singleFileTorLoc = "src/test/resources/pieces_test_files/seeder/torrent_files/single.torrent";
        this.multiFile1TorLoc = "src/test/resources/pieces_test_files/seeder/torrent_files/multi1.torrent";
        this.multiFile2TorLoc = "src/test/resources/pieces_test_files/seeder/torrent_files/multi2.torrent";
    }

    @Test
    public void testSeederHasAllPieces() throws Exception {
        PiecesHandler ph = new PiecesHandler(TorrentParser.parseTorrent(singleFileTorLoc), seederDownloadFolder);
        assertEquals(36, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(36), ph.getExistingPieceIndexes());

        ph = new PiecesHandler(TorrentParser.parseTorrent(multiFile1TorLoc), seederDownloadFolder);
        assertEquals(5, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(5), ph.getExistingPieceIndexes());

        ph = new PiecesHandler(TorrentParser.parseTorrent(multiFile2TorLoc), seederDownloadFolder);
        assertEquals(4, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(4), ph.getExistingPieceIndexes());
    }

    private Set<Integer> nNumbersToSet(int n) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < n; i++) {
            set.add(i);
        }
        return set;
    }
}

