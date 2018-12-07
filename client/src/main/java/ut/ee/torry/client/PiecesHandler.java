package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentFile;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import static ut.ee.torry.client.util.PiecesUtil.calcBytesCount;

public class PiecesHandler {

    private static final Logger log = LoggerFactory.getLogger(TorrentTask.class);

    private final Torrent torrent;
    private final String downloadFileDir;
    private final int pieceSize;
    private final int piecesCount;
    private final long totalSize;

    private final boolean[] bitField;

    public PiecesHandler(Torrent torrent, String downloadFileDirPath) {
        this.torrent = Objects.requireNonNull(torrent);
        this.downloadFileDir = Objects.requireNonNull(downloadFileDirPath);
        this.pieceSize = torrent.getPieceLength().intValue();
        this.piecesCount = torrent.getPieces().size();
        this.bitField = findBitField();
        if (torrent.getTotalSize() != null) {
            this.totalSize = torrent.getTotalSize();
        } else {
            long tempSize = 0;
            for (TorrentFile torrentFile : torrent.getFileList()) {
                tempSize += torrentFile.getFileLength();
            }
            totalSize = tempSize;
        }
    }

    public long getTotalSize() {
        return totalSize;
    }

    public long getBytesDownloaded() {
        long existingPiecesSize = 0;
        for (int i = 0; i < bitField.length; i++) {
            if (bitField[i]) {
                if (i == bitField.length - 1) { // Last piece
                    existingPiecesSize += (totalSize - (pieceSize * (piecesCount - 1)));
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

    public synchronized boolean writePiece(int id, byte[] bytes) throws IOException {
        Piece piece = new Piece(id, this.torrent, bytes, this.downloadFileDir);
        if (piece.isValid()) {
            piece.writeBytes();
            this.bitField[id] = true;
            return true;
        } else {
            log.error("You are trying to write not correct bytes for piece {}", id);
            return false;
        }
    }

    private boolean[] findBitField() {
        String fullPath = this.downloadFileDir + File.separator + this.torrent.getName();
        File downloadedTorrent = new File(fullPath);
        if (downloadedTorrent.isDirectory()) {
            return findBitFieldFromDirectory(fullPath);
        } else if (downloadedTorrent.isFile()) {
            return findBitFieldFromSingleFile(fullPath);
        } else { // such file/folder does not exist
            boolean[] bitField = new boolean[piecesCount];
            Arrays.fill(bitField, false);
            return bitField;
        }
    }

    private boolean[] findBitFieldFromDirectory(String folderLoc) {
        try {
            boolean[] bitField = new boolean[piecesCount];
            int currentPiece = 0;
            int bytesReadForPiece = 0;
            byte[] pieceBytes = new byte[0];

            for (TorrentFile torrentFile : torrent.getFileList()) {
                String filepath = folderLoc + File.separator + String.join(File.separator, torrentFile.getFileDirs());
                long bytesReadFromFile = 0;
                RandomAccessFile file = new RandomAccessFile(filepath, "r");

                // reading bytes from while according to piece size until all bytes from the file are read
                while (bytesReadFromFile != file.length()) {
                    int nrOfBytesToRead = calcBytesCount(file.getFilePointer(), bytesReadForPiece, pieceSize, file.length());
                    byte[] bytes = new byte[nrOfBytesToRead];
                    file.read(bytes);
                    bytesReadForPiece += nrOfBytesToRead;
                    bytesReadFromFile += nrOfBytesToRead;
                    pieceBytes = ArrayUtils.addAll(pieceBytes, bytes);

                    if (bytesReadForPiece == pieceSize) {  // we have a full piece
                        Piece piece = new Piece(currentPiece, this.torrent, pieceBytes, this.downloadFileDir);
                        pieceBytes = new byte[0];
                        bitField[currentPiece] = piece.isValid();
                        currentPiece++;
                        bytesReadForPiece = 0;
                    }
                }
                file.close();

                if (!Arrays.equals(pieceBytes, new byte[0])) {  // if last piece isn't exactly full, then we need to add it
                    Piece piece = new Piece(currentPiece, this.torrent, pieceBytes, this.downloadFileDir);
                    bitField[currentPiece] = piece.isValid();
                }
            }
            return bitField;
        } catch (IOException e) {
            boolean[] bitField = new boolean[piecesCount];
            Arrays.fill(bitField, false);
            return bitField;
        }
    }

    private boolean[] findBitFieldFromSingleFile(String filepath) {
        try {
            boolean[] bitField = new boolean[piecesCount];
            RandomAccessFile file = new RandomAccessFile(filepath, "r");
            byte[] bytes;

            for (int i = 0; i < piecesCount; i++) {
                int bytesCount = calcBytesCount(file.getFilePointer(), pieceSize, file.length());
                bytes = new byte[bytesCount];
                file.read(bytes);
                Piece piece = new Piece(i, this.torrent, bytes, this.downloadFileDir);

                // verifying if the bytes really correspond to torrent file metadata
                bitField[i] = piece.isValid();
            }
            file.close();
            return bitField;
        } catch (IOException e) {  // it means that the file doesn't exist, so all bits are 0.
            boolean[] bitField = new boolean[piecesCount];
            Arrays.fill(bitField, false);
            return bitField;
        }
    }

    private Piece getPieceByIdForSingleFile(int id) throws IOException {
        long fromBytes = (long) this.pieceSize * (long) id;
        String filePath = downloadFileDir + File.separator + this.torrent.getName();
        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        file.seek(fromBytes);

        int numberOfBytesBeforeFileEnd = calcBytesCount(file.getFilePointer(), pieceSize, file.length());
        byte[] currentPieceBytes = new byte[numberOfBytesBeforeFileEnd];
        file.read(currentPieceBytes);
        file.close();
        return returnPiece(new Piece(id, this.torrent, currentPieceBytes, this.downloadFileDir));
    }

    private Piece getPieceByIdForDirectory(int id) throws IOException {
        long fromByte = (long) this.pieceSize * (long) id;
        long currentByte = 0;
        byte[] pieceBytes = new byte[0];

        for (TorrentFile torrentFile : torrent.getFileList()) {
            // we have to take some bytes from that file
            if (currentByte + pieceBytes.length + torrentFile.getFileLength() >= fromByte) {
                String filePath = downloadFileDir + File.separator + this.torrent.getName() +
                        File.separator + String.join(File.separator, torrentFile.getFileDirs());
                long pieceStartByteInFile = fromByte + pieceBytes.length - currentByte;

                RandomAccessFile file = new RandomAccessFile(filePath, "r");
                int nrOfBytesToRead = calcBytesCount(pieceStartByteInFile, pieceBytes.length, pieceSize, file.length());
                file.seek(pieceStartByteInFile);
                byte[] currentFileBytes = new byte[nrOfBytesToRead];
                file.read(currentFileBytes);
                file.close();
                pieceBytes = ArrayUtils.addAll(pieceBytes, currentFileBytes);

                if (pieceBytes.length == pieceSize) {
                    return returnPiece(new Piece(id, this.torrent, pieceBytes, this.downloadFileDir));
                }
            }
            currentByte += torrentFile.getFileLength();
        }
        // adding last piece which might not be with complete length
        return returnPiece(new Piece(id, this.torrent, pieceBytes, this.downloadFileDir));
    }

    private Piece returnPiece(Piece piece) {
        if (piece.isValid()) {
            return piece;
        } else {
            throw new AssertionError("Piece is either not downloaded or there's a mistake in the code");
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
