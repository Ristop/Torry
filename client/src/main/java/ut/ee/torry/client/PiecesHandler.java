package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentFile;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class PiecesHandler {

    private static final Logger log = LoggerFactory.getLogger(PiecesHandler.class);

    private final Torrent torrent;
    private final String downloadFileDir;
    private final int pieceSize;
    private final int piecesCount;

    private final Set<Integer> existingPieceIndexes;
    private final Set<Integer> notExistingPieceIndexes;

    public PiecesHandler(Torrent torrent, String downloadFileDirPath) throws IOException {
        this.torrent = Objects.requireNonNull(torrent);
        this.downloadFileDir = Objects.requireNonNull(downloadFileDirPath);
        this.pieceSize = torrent.getPieceLength().intValue();
        this.piecesCount = torrent.getPieces().size();
        this.existingPieceIndexes = findAvailablePieceIndexes();
        this.notExistingPieceIndexes = findNotAvailablePieceIndexes(existingPieceIndexes);
    }

    public long getBytesDownloaded() {
        long existingPiecesSize = existingPieceIndexes.size() * pieceSize - 1;

        // If we have last piece
        if (existingPieceIndexes.contains(piecesCount - 1)) {
            existingPiecesSize += torrent.getTotalSize() - existingPiecesSize;
        }
        return existingPiecesSize;
    }

    public Set<Integer> getExistingPieceIndexes() {
        return this.existingPieceIndexes;
    }

    public Set<Integer> getNotExistingPieceIndexes() {
        return this.notExistingPieceIndexes;
    }

    public int getPiecesCount() {
        return this.piecesCount;
    }

    public boolean hasPiece(int index) {
        return this.existingPieceIndexes.contains(index);
    }

    public Piece getPiece(int id) throws IOException {
        if (torrent.isSingleFileTorrent()) {
            return getPieceByIdForSingleFile(id);
        } else {
            return getPieceByIdForDirectory(id);
        }
    }

    public byte[] getPieceBytes(int id) throws IOException {
        return getPiece(id).getBytes();
    }

    public void writePiece(int id, byte[] bytes) throws IOException {
        Piece piece = new Piece(id, this.torrent, bytes);
        piece.writeBytes(this.downloadFileDir);
        if (piece.isValid()) {
            this.notExistingPieceIndexes.remove(id);
            this.existingPieceIndexes.add(id);
        } else {
            throw new IllegalStateException("You are trying to write not correct bytes");
        }
    }

    private Set<Integer> findNotAvailablePieceIndexes(Set<Integer> existingPieces) {
        Set<Integer> notExistingPieces = new HashSet<>();
        for (int i = 0; i < this.piecesCount; i++) {
            if (!existingPieces.contains(i)) {
                notExistingPieces.add(i);
            }
        }
        return notExistingPieces;
    }

    private Set<Integer> findAvailablePieceIndexes() throws IOException {
        String fullPath = this.downloadFileDir + File.separator + this.torrent.getName();
        File downloadedTorrent = new File(fullPath);

        if (downloadedTorrent.isDirectory()) {
            byte[] dirTotalBytes = getDictionaryBytes(fullPath);
            return findAvailablePieceIndexes(dirTotalBytes);
        } else if (downloadedTorrent.isFile()) {
            byte[] fileBytes = getFileBytes(downloadedTorrent.toPath());
            return findAvailablePieceIndexes(fileBytes);
        } else {
            return new HashSet<>();
        }
    }

    private Set<Integer> findAvailablePieceIndexes(byte[] totalBytes) {
        Set<Integer> existing = new HashSet<>();

        for (int i = 0; i < piecesCount; i++) {
            int fromBytes = this.pieceSize * i;

            int toBytes;
            if (piecesCount - 1 == i) { // If it's the last piece
                toBytes = totalBytes.length;
            } else {
                toBytes = this.pieceSize * (i + 1);
            }

            byte[] pieceBytes = Arrays.copyOfRange(totalBytes, fromBytes, toBytes);

            Piece piece = new Piece(i, this.torrent, pieceBytes);
            if (piece.isValid()) { // verifying if the bytes really correspond to torrent file metadata
                existing.add(piece.getId());
            }
        }

        return existing;
    }

    private byte[] getDictionaryBytes(String dirPath) throws IOException {
        byte[] bytes = new byte[0];

        for (TorrentFile torrentFile : torrent.getFileList()) {
            Path path = Paths.get(dirPath, torrentFile.getFileDirs().toArray(new String[0]));

            if (Files.exists(path)) {
                byte[] fileContent = Files.readAllBytes(path);
                bytes = ArrayUtils.addAll(bytes, fileContent);
            } else {
                // TODO : what if file length is Long?
                byte[] emptyBytes = new byte[torrentFile.getFileLength().intValue()];
                bytes = ArrayUtils.addAll(bytes, emptyBytes);
            }
        }
        return bytes;
    }

    private byte[] getFileBytes(Path path) throws IOException {
        return Files.readAllBytes(path);
    }

    private Piece getPieceByIdForSingleFile(int id) throws IOException {
        byte[] fileContent = Files.readAllBytes(Paths.get(this.downloadFileDir, torrent.getName()));

        int fromBytes = this.pieceSize * id;

        int toBytes;
        if (piecesCount - 1 == id) { // If it's the last piece
            toBytes = fileContent.length;
        } else {
            toBytes = this.pieceSize * (id + 1);
        }

        byte[] currentPieceBytes = Arrays.copyOfRange(fileContent, fromBytes, toBytes);

        Piece piece = new Piece(id, this.torrent, currentPieceBytes);

        if (piece.isValid()) {
            return piece;
        } else {
            throw new IllegalStateException("Piece is either not downloaded or there's a mistake in the code");
        }
    }

    private Piece getPieceByIdForDirectory(int id) throws IOException {
        String fullPath = this.downloadFileDir + File.separator + this.torrent.getName();
        byte[] dirContent = getDictionaryBytes(fullPath);
        return getPieceByIdFromBytes(dirContent, id);
    }

    private Piece getPieceByIdFromBytes(byte[] content, int id) {
        int endIndex = this.torrent.getPieceLength().intValue() * (id + 1);
        if (endIndex > content.length) {  // last piece is not full piece
            endIndex = content.length;
        }
        byte[] currentPieceBytes = Arrays.copyOfRange(
                content,
                this.torrent.getPieceLength().intValue() * id,
                endIndex
        );

        Piece piece = new Piece(id, this.torrent, currentPieceBytes);
        if (piece.isValid()) {
            return piece;
        } else {
            throw new IllegalStateException("Piece is either not downloaded or there's a mistake in " +
                    "the code with piece " + "id = " + id);
        }
    }

}
