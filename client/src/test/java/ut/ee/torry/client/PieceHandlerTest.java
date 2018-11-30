package ut.ee.torry.client;

import be.christophedetroyer.torrent.TorrentParser;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

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

    private static final String SEEDER_DOWNLOAD_FOLDER = "src/test/resources/pieces_test_files/seeder/downloads";
    private static final String DOWNLOADER_DOWNLOAD_FOLDER = "src/test/resources/pieces_test_files/downloader/downloads";
    private static final String SINGLE_FILE_TOR_LOC = "src/test/resources/pieces_test_files/seeder/torrent_files/single.torrent";
    private static final String MULTI_FILE_1_TOR_LOC = "src/test/resources/pieces_test_files/seeder/torrent_files/multi1.torrent";
    private static final String MULTI_FILE_2_TOR_LOC = "src/test/resources/pieces_test_files/seeder/torrent_files/multi2.torrent";

    @Test
    public void testSeederHasAllPieces() throws Exception {
        PiecesHandler ph = new PiecesHandler(TorrentParser.parseTorrent(SINGLE_FILE_TOR_LOC), SEEDER_DOWNLOAD_FOLDER);
        assertEquals(36, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(36), ph.getExistingPieceIndexes());

        ph = new PiecesHandler(TorrentParser.parseTorrent(MULTI_FILE_1_TOR_LOC), SEEDER_DOWNLOAD_FOLDER);
        assertEquals(5, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(5), ph.getExistingPieceIndexes());

        ph = new PiecesHandler(TorrentParser.parseTorrent(MULTI_FILE_2_TOR_LOC), SEEDER_DOWNLOAD_FOLDER);
        assertEquals(4, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(4), ph.getExistingPieceIndexes());
    }

    @Test
    public void testDownloaderHasNoPieces() throws Exception {
        PiecesHandler ph = new PiecesHandler(TorrentParser.parseTorrent(SINGLE_FILE_TOR_LOC), DOWNLOADER_DOWNLOAD_FOLDER);
        assertEquals(0, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(0), ph.getExistingPieceIndexes());

        ph = new PiecesHandler(TorrentParser.parseTorrent(MULTI_FILE_1_TOR_LOC), DOWNLOADER_DOWNLOAD_FOLDER);
        assertEquals(0, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(0), ph.getExistingPieceIndexes());

        ph = new PiecesHandler(TorrentParser.parseTorrent(MULTI_FILE_2_TOR_LOC), DOWNLOADER_DOWNLOAD_FOLDER);
        assertEquals(0, ph.getExistingPieceIndexes().size());
        assertEquals(nNumbersToSet(0), ph.getExistingPieceIndexes());
    }

    @Test
    public void testSendSingleFilePieceByPiece() throws Exception {
        PiecesHandler downloader = new PiecesHandler(TorrentParser.parseTorrent(SINGLE_FILE_TOR_LOC), DOWNLOADER_DOWNLOAD_FOLDER);
        PiecesHandler sender = new PiecesHandler(TorrentParser.parseTorrent(SINGLE_FILE_TOR_LOC), SEEDER_DOWNLOAD_FOLDER);
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

        String seededPath = SEEDER_DOWNLOAD_FOLDER + File.separator + "singleFile.txt";
        String downloadedPath = DOWNLOADER_DOWNLOAD_FOLDER + File.separator + "singleFile.txt";

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
        testSendMultiFilePieceByPiece(5, MULTI_FILE_1_TOR_LOC);
        testCorrectnessOfMUltiFileDownload("multi1");
    }


    @Test
    public void testCorrectnessOfMultiFile2Downlad() throws Exception {
        testSendMultiFilePieceByPiece(4, MULTI_FILE_2_TOR_LOC);
        testCorrectnessOfMUltiFileDownload("multi2");
    }

    private void testSendMultiFilePieceByPiece(int pieceCount, String torrentLocation) throws Exception {
        PiecesHandler downloader = new PiecesHandler(TorrentParser.parseTorrent(torrentLocation), DOWNLOADER_DOWNLOAD_FOLDER);
        PiecesHandler sender = new PiecesHandler(TorrentParser.parseTorrent(torrentLocation), SEEDER_DOWNLOAD_FOLDER);
        Set<Integer> downloaded = nNumbersToSet(0);
        Set<Integer> needsDownloading = nNumbersToSet(pieceCount);
        assertEquals(downloaded, downloader.getExistingPieceIndexes());
        assertEquals(needsDownloading, downloader.getNotExistingPieceIndexes());

        sendPieceByPiece(downloader, sender, downloaded, needsDownloading);
    }

    private void testCorrectnessOfMUltiFileDownload(String name) throws IOException {
        String seederFolderPath = SEEDER_DOWNLOAD_FOLDER + File.separator + name;
        String downloadFolderPath = DOWNLOADER_DOWNLOAD_FOLDER + File.separator + name;
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

    @AfterEach
    public void deleteMadeFiles() throws IOException {
        FileUtils.cleanDirectory(new File(DOWNLOADER_DOWNLOAD_FOLDER));
    }
}

