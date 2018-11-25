package ut.ee.torry.client;

import be.christophedetroyer.torrent.TorrentParser;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PieceHandlerTest {

    private String seederDownloadFolder;
    private String downloaderDownloadFolder;
    private String singleFileTorLoc;
    private String multiFile1TorLoc;
    private String multiFile2TorLoc;

    @Before
    public void setDownloadFolders() {
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

    @Test
    public void testDownloaderHasNoPieces() throws Exception {
        PiecesHandler ph = new PiecesHandler(TorrentParser.parseTorrent(singleFileTorLoc), downloaderDownloadFolder);
        assertEquals(0, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(0), ph.getExistingPieceIndexes());

        ph = new PiecesHandler(TorrentParser.parseTorrent(multiFile1TorLoc), downloaderDownloadFolder);
        assertEquals(0, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(0), ph.getExistingPieceIndexes());

        ph = new PiecesHandler(TorrentParser.parseTorrent(multiFile2TorLoc), downloaderDownloadFolder);
        assertEquals(0, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(0), ph.getExistingPieceIndexes());
    }

    @Test
    public void testSendSingleFilePieceByPiece() throws Exception {
        PiecesHandler downloader = new PiecesHandler(TorrentParser.parseTorrent(singleFileTorLoc), downloaderDownloadFolder);
        PiecesHandler sender = new PiecesHandler(TorrentParser.parseTorrent(singleFileTorLoc), seederDownloadFolder);
        Set<Integer> downloaded = nNumbersToSet(0);
        Set<Integer> needsDownloading = nNumbersToSet(36);
        assertEquals(downloaded, downloader.getExistingPieceIndexes());
        assertEquals(needsDownloading, downloader.getNotExistingPieceIndexes());

        sendPieceByPiece(downloader, sender, downloaded, needsDownloading);
    }


    private void sendPieceByPiece(PiecesHandler downloader, PiecesHandler sender, Set<Integer> downloaded,
                                  Set<Integer> needsDownloading) throws IOException {
        while (needsDownloading.size() != 0) {
            int randomPieceID = getRandomPieceID(needsDownloading);
            byte[] pieceBytes = sender.getPieceBytes(randomPieceID);
            downloader.writePiece(randomPieceID, pieceBytes);
            downloaded.add(randomPieceID);
            needsDownloading.remove(randomPieceID);
            assertEquals(downloaded, downloader.getExistingPieceIndexes());
            assertEquals(needsDownloading, downloader.getNotExistingPieceIndexes());
        }
    }

    @Test
    public void testCorrectnessOfSingleFileDownlad() throws Exception {
        testSendSingleFilePieceByPiece();

        String seededPath = seederDownloadFolder + File.separator + "singleFile.txt";
        String downloadedPath = downloaderDownloadFolder + File.separator + "singleFile.txt";

        File seeded = new File(seededPath);
        File downloaded = new File(downloadedPath);
        byte[] seederBytes = Files.readAllBytes(seeded.toPath());
        byte[] downloaderBytes = Files.readAllBytes(downloaded.toPath());
        assertArrayEquals(seederBytes, downloaderBytes);

        List<String> seededLines = Files.readAllLines(Paths.get(seededPath));
        List<String> downloadedLines = Files.readAllLines(Paths.get(downloadedPath));
        assertEquals(seededLines, downloadedLines);
    }




    @Test
    public void testCorrectnessOfMultiFile1Downlad() throws Exception {
        testSendMultiFilePieceByPiece(5, multiFile1TorLoc);
        testCorrectnessOfMUltiFileDownload("multi1");
    }


    @Test
    public void testCorrectnessOfMultiFile2Downlad() throws Exception {
        testSendMultiFilePieceByPiece(4, multiFile2TorLoc);
        testCorrectnessOfMUltiFileDownload("multi2");
    }

    private void testSendMultiFilePieceByPiece(int pieceCount, String torrentLocation) throws Exception {
        PiecesHandler downloader = new PiecesHandler(TorrentParser.parseTorrent(torrentLocation), downloaderDownloadFolder);
        PiecesHandler sender = new PiecesHandler(TorrentParser.parseTorrent(torrentLocation), seederDownloadFolder);
        Set<Integer> downloaded = nNumbersToSet(0);
        Set<Integer> needsDownloading = nNumbersToSet(pieceCount);
        assertEquals(downloaded, downloader.getExistingPieceIndexes());
        assertEquals(needsDownloading, downloader.getNotExistingPieceIndexes());

        sendPieceByPiece(downloader, sender, downloaded, needsDownloading);
    }

    private void testCorrectnessOfMUltiFileDownload(String name) throws IOException {
        String seederFolderPath = seederDownloadFolder + File.separator + name;
        String downloadFolderPath = downloaderDownloadFolder + File.separator + name;
        File seederFol = new File(seederFolderPath);

        File seededFile;
        File downloadedFile;
        for (File file : Objects.requireNonNull(seederFol.listFiles())) {
            String seededPath = seederFolderPath + File.separator + file.getName();
            String downloadedPath = downloadFolderPath + File.separator + file.getName();
            seededFile = new File(seededPath);
            downloadedFile = new File(downloadedPath);

            byte[] seederBytes = Files.readAllBytes(seededFile.toPath());
            byte[] downloaderBytes = Files.readAllBytes(downloadedFile.toPath());
            assertArrayEquals(seederBytes, downloaderBytes);

            String seededText = new String(seederBytes, StandardCharsets.UTF_8);
            String downladedText = new String(seederBytes, StandardCharsets.UTF_8);
            assertEquals(seededText, downladedText);
        }
    }

    private Set<Integer> nNumbersToSet(int n) {
        Set<Integer> set = new HashSet<>();
        for (int i = 0; i < n; i++) {
            set.add(i);
        }
        return set;
    }

    private int getRandomPieceID(Set<Integer> list) {
        if (list.size() >= 1) {
            int size = list.size();
            int item = new Random().nextInt(size);
            int i = 0;
            for (Integer obj : list) {
                if (i == item) {
                    return obj;
                }
                i++;
            }
        }
        return -1;
    }

    @After
    public void deleteMadeFiles() throws IOException {
        FileUtils.cleanDirectory(new File(downloaderDownloadFolder));
    }
}

