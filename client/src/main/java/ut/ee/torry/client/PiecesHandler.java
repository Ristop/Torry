package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentFile;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    private final Set<Integer> existingPieces;
    private final Set<Integer> notExistingPieces;

    public PiecesHandler(Torrent torrent, String downloadFileDirPath) throws IOException {
        this.torrent = Objects.requireNonNull(torrent);
        this.downloadFileDir = Objects.requireNonNull(downloadFileDirPath);
        this.pieceSize = torrent.getPieceLength().intValue();
        this.piecesCount = findPiecesCount();
        this.existingPieces = findAvailablePieceIndexes();
        this.notExistingPieces = findNotAvailablePieceIndexes(existingPieces);
    }

    public Set<Integer> getExistingPieces() {
        return this.existingPieces;
    }

    public Set<Integer> getNotExistingPieces() {
        return this.notExistingPieces;
    }

    public int getPiecesCount() {
        return this.piecesCount;
    }

    public Piece getPiece(int id) throws IOException {
        if (torrent.isSingleFileTorrent()) {
            return getPieceByIdForSingleFile(id);
        } else {
            // TODO: multiple file torrent case
            throw new NotImplementedException("Multi file torrent getPiece not yet implemented");
        }
    }

    public byte[] getPieceBytes(int id) throws IOException {
        return getPiece(id).getBytes();
    }

    public void writePiece(int id, byte[] bytes) {
        Piece piece = new Piece(id, this.torrent, bytes);
        piece.writeBytes(this.downloadFileDir);
        if (piece.isValid()) {
            this.notExistingPieces.remove(id);
            this.existingPieces.add(id);
        }
    }

    private Set<Integer> findNotAvailablePieceIndexes(Set<Integer> existingPieces) {
        Set<Integer> notExistingPieces = new HashSet<>();
        for (int i = 0; i < this.piecesCount; i++) {
            if (existingPieces.contains(i)) {
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
            byte[] fileBytes = getFileBytes(downloadedTorrent);
            return findAvailablePieceIndexes(fileBytes);
        } else {
            return new HashSet<>();
        }
    }

    private Set<Integer> findAvailablePieceIndexes(byte[] totalBytes) {
        Set<Integer> existing = new HashSet<>();

        byte[] currentPieceBytes;
        for (int pieceIndex = 0; pieceIndex < this.piecesCount; pieceIndex++) {
            if ((pieceSize * (pieceIndex + 1)) <= totalBytes.length) { // last piece might not be exactly as long as other pieces
                currentPieceBytes = Arrays.copyOfRange(
                        totalBytes,
                        pieceSize * pieceIndex,
                        (pieceSize * (pieceIndex + 1))
                );
            } else {
                currentPieceBytes = Arrays.copyOfRange(
                        totalBytes,
                        pieceSize * pieceIndex,
                        totalBytes.length
                );
            }

            Piece piece = new Piece(pieceIndex, this.torrent, currentPieceBytes);
            if (piece.isValid()) { // verifying if the bytes really correspond to torrent file metadata
                existing.add(piece.getId());
            }
        }
        return existing;
    }

    private byte[] getDictionaryBytes(String dirPath) {
        byte[] totalBytes = null;
        for (TorrentFile torrentFile : torrent.getFileList()) {
            String fileLoc = String.join(File.separator, torrentFile.getFileDirs());
            File file = new File(dirPath + File.separator + fileLoc);
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                if (totalBytes == null) {
                    totalBytes = fileContent;
                } else {
                    totalBytes = ArrayUtils.addAll(totalBytes, fileContent);
                }
            } catch (IOException e) { // that file does not exist
                byte[] fileContent = new byte[torrentFile.getFileLength().intValue()];
                if (totalBytes == null) {
                    totalBytes = fileContent;
                } else {
                    totalBytes = ArrayUtils.addAll(totalBytes, fileContent);
                }
            }
        }
        return totalBytes;
    }

    private byte[] getFileBytes(File file) throws IOException {
        return Files.readAllBytes(file.toPath());
    }

    private int findPiecesCount() {
        if (this.piecesCount == 0) {
            double pieceSize = this.torrent.getPieceLength();
            if (this.torrent.isSingleFileTorrent()) {
                return (int) (Math.ceil((this.torrent.getTotalSize().doubleValue() / pieceSize)));
            } else {
                double totalSize = 0;
                for (TorrentFile file : this.torrent.getFileList()) {
                    totalSize = totalSize + file.getFileLength().intValue();
                }
                return (int) Math.ceil(totalSize / this.torrent.getPieceLength().doubleValue());
            }
        } else {
            return this.piecesCount;
        }
    }

    private Piece getPieceByIdForSingleFile(int id) throws IOException {
        File file = new File(this.downloadFileDir + File.separator + this.torrent.getName());
        byte[] fileContent = Files.readAllBytes(file.toPath());
        byte[] currentPieceBytes = Arrays.copyOfRange(
                fileContent,
                this.torrent.getPieceLength().intValue() * id,
                this.torrent.getPieceLength().intValue() * (id + 1)
        );
        Piece piece = new Piece(id, this.torrent, currentPieceBytes);
        if (piece.isValid()) {
            return piece;
        } else {
            throw new IllegalStateException("Piece is either not downloaded or there's a mistake in the code");
        }
    }

}
