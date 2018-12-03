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
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class PiecesHandler {

    private static final Logger log = LoggerFactory.getLogger(PiecesHandler.class);

    private final Torrent torrent;
    private final String downloadFileDir;
    private final int pieceSize;
    private final int piecesCount;

    private final byte[] existingBytes;

    private final boolean[] bitField;

    public PiecesHandler(Torrent torrent, String downloadFileDirPath) throws IOException {
        this.torrent = Objects.requireNonNull(torrent);
        this.downloadFileDir = Objects.requireNonNull(downloadFileDirPath);
        this.pieceSize = torrent.getPieceLength().intValue();
        this.piecesCount = torrent.getPieces().size();

        String fullPath = this.downloadFileDir + File.separator + this.torrent.getName();
        File downloadedTorrent = new File(fullPath);

        if (downloadedTorrent.isDirectory()) {
            this.existingBytes = getDictionaryBytes(fullPath);
        } else if (downloadedTorrent.isFile()) {
            this.existingBytes = getFileBytes(downloadedTorrent.toPath());
        } else {
            int numOfBytes = 0;
            List<TorrentFile> fileList = torrent.getFileList();
            if (fileList != null) {
                for (TorrentFile torrentFile : fileList) {
                    numOfBytes += torrentFile.getFileLength();
                }
            } else {
                numOfBytes = Math.toIntExact(torrent.getTotalSize());
            }

            this.existingBytes = new byte[numOfBytes];
        }

        this.bitField = findBitField();
    }

    public long getBytesDownloaded() {
        long existingPiecesSize = 0;
        for (int i = 0; i < bitField.length; i++) {
            if (bitField[i]) {
                if (i == bitField.length - 1) { // Last piece
                    existingPiecesSize += (torrent.getTotalSize() - (pieceSize * (piecesCount - 1)));
                } else { // Regular sized piece
                    existingPiecesSize += pieceSize;
                }
            }
        }
        return existingPiecesSize;
    }

    public boolean[] getBitField() {
        return bitField;
    }

    public boolean hasPiece(int index) {
        return bitField[index];
    }

    public Piece getPiece(int id) {
        if (torrent.isSingleFileTorrent()) {
            return getPieceByIdForSingleFile(id);
        } else {
            return getPieceByIdForDirectory(id);
        }
    }

    public byte[] getPieceBytes(int id) {
        return getPiece(id).getBytes();
    }

    public synchronized void writePiece(int id, byte[] bytes) throws IOException {
        Piece piece = new Piece(id, this.torrent, bytes, this.downloadFileDir);
        if (piece.isValid()) {
            piece.writeBytes(this.existingBytes);
            this.bitField[id] = true;
        } else {
            throw new IllegalStateException("You are trying to write not correct bytes");
        }
    }

    private boolean[] findBitField() {
        boolean[] bitField = new boolean[piecesCount];

        for (int i = 0; i < piecesCount; i++) {
            int fromBytes = this.pieceSize * i;

            int toBytes;
            if (piecesCount - 1 == i) { // If it's the last piece
                toBytes = this.existingBytes.length;
            } else {
                toBytes = this.pieceSize * (i + 1);
            }

            byte[] pieceBytes = Arrays.copyOfRange(this.existingBytes, fromBytes, toBytes);

            Piece piece = new Piece(i, this.torrent, pieceBytes, this.downloadFileDir);

            // verifying if the bytes really correspond to torrent file metadata
            bitField[i] = piece.isValid();
        }

        return bitField;
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

    private Piece getPieceByIdForSingleFile(int id) {
        int fromBytes = this.pieceSize * id;

        int toBytes;
        if (piecesCount - 1 == id) { // If it's the last piece
            toBytes = this.existingBytes.length;
        } else {
            toBytes = this.pieceSize * (id + 1);
        }

        byte[] currentPieceBytes = Arrays.copyOfRange(this.existingBytes, fromBytes, toBytes);

        Piece piece = new Piece(id, this.torrent, currentPieceBytes, this.downloadFileDir);

        if (piece.isValid()) {
            return piece;
        } else {
            throw new AssertionError("Piece is either not downloaded or there's a mistake in the code");
        }
    }

    private Piece getPieceByIdForDirectory(int id) {
        int endIndex = this.torrent.getPieceLength().intValue() * (id + 1);
        if (endIndex > this.existingBytes.length) {  // last piece is not full piece
            endIndex = this.existingBytes.length;
        }
        byte[] currentPieceBytes = Arrays.copyOfRange(
                this.existingBytes,
                this.torrent.getPieceLength().intValue() * id,
                endIndex
        );

        Piece piece = new Piece(id, this.torrent, currentPieceBytes, this.downloadFileDir);
        if (piece.isValid()) {
            return piece;
        } else {
            throw new IllegalStateException("Piece is either not downloaded or there's a mistake in " +
                    "the code with piece " + "id = " + id);
        }
    }

    public Set<Integer> getNotExistingPieceIndexes() {
        Set<Integer> notExistingPieces = new HashSet<>();
        for (int i = 0; i < bitField.length; i++) {
            if (!bitField[i]) {
                notExistingPieces.add(i);
            }
        }
        return notExistingPieces;
    }

    public Set<Integer> getExistingPieceIndexes() {
        Set<Integer> existingPieces = new HashSet<>();
        for (int i = 0; i < bitField.length; i++) {
            if (bitField[i]) {
                existingPieces.add(i);
            }
        }
        return existingPieces;
    }

}
