package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentFile;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
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
                int bytesReadFromFile = 0;
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

                if (!pieceBytes.equals(new byte[0])) {  // if last piece isn't exactly full, then we need to add it
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

    private int calcBytesCount(long currentPosition, int pieceSize, long fileLength) {
        if (currentPosition + pieceSize > fileLength) {
            return (int) (fileLength - currentPosition);
        } else {
            return pieceSize;
        }
    }

    private int calcBytesCount(long currentPosition, int bytesAlreadyRead, int pieceSize, long fileLength) {
        long bytesToRead = fileLength - currentPosition;
        if (bytesToRead + bytesAlreadyRead > pieceSize){  //can fill the whole piece (can't use all new bytes)
            return pieceSize - bytesAlreadyRead;
        } else { // can use all new bytes
            return (int) bytesToRead;
        }
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
        int fromBytes = this.pieceSize * id;
        String filePath = downloadFileDir + File.separator + this.torrent.getName();
        RandomAccessFile file = new RandomAccessFile(filePath, "r");
        file.seek(fromBytes);

        int numberOfBytesBeforeFileEnd = (int) (this.torrent.getTotalSize() - fromBytes);
        byte[] currentPieceBytes;
        if (numberOfBytesBeforeFileEnd < this.pieceSize)
            currentPieceBytes = new byte[numberOfBytesBeforeFileEnd];
        else
            currentPieceBytes = new byte[this.pieceSize];

        file.read(currentPieceBytes);
        file.close();
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
