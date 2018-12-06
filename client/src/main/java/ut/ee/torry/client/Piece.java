package ut.ee.torry.client;

import be.christophedetroyer.bencoding.Utils;
import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import static ut.ee.torry.client.util.PiecesUtil.calcBytesCount;


public class Piece {

    private final int id;
    private final byte[] bytes;
    private final Torrent torrent;
    private final String hash;
    private final String path;
    private final File file;

    public Piece(int id, Torrent torrent, byte[] bytes, String clientPath) {
        this.id = id;
        this.bytes = bytes;
        this.torrent = torrent;
        this.hash = torrent.getPieces().get(id);
        this.path = clientPath + File.separator + this.torrent.getName();
        this.file = new File(path);
    }

    public int getId() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public boolean isValid() {
        String calculatedHash = Utils.bytesToHex(DigestUtils.sha1(this.bytes));
        return hash.equals(calculatedHash);
    }

    public void writeBytes() throws IOException {
        if (torrent.isSingleFileTorrent()) {
            try {
                writeBytesToFile();
            } catch (IOException e) {
                createFile();
                writeBytesToFile();
            }
        } else {
            try {
                writeBytesToDir();
            } catch (IOException e) {
                createDirectory();
                writeBytesToDir();
            }
        }
    }

    private void createFile() throws IOException {
        byte[] defaultContent = new byte[torrent.getTotalSize().intValue()];
        try (OutputStream os = new FileOutputStream(new File(this.path))) {
            os.write(defaultContent);
        }
    }

    private void writeBytesToFile() throws IOException {
        long pieceStartLoc = torrent.getPieceLength() * id;
        RandomAccessFile file = new RandomAccessFile(this.path, "rw");
        file.seek(pieceStartLoc);
        file.write(this.bytes);
        file.close();
    }

    private void createDirectory() throws IOException {
        boolean success = this.file.mkdir();
        if (success) {
            for (TorrentFile torrentFile : this.torrent.getFileList()) {
                if (torrentFile.getFileDirs().size() > 1) {
                    createSubFoldersIFNeeded(this.file.getPath(), torrentFile.getFileDirs());
                }
                String fileLocInDir = String.join(File.separator, torrentFile.getFileDirs());
                File subFile = new File(this.file.getPath() + File.separator + fileLocInDir);
                byte[] defaultContent = new byte[torrentFile.getFileLength().intValue()];
                try (OutputStream os = new FileOutputStream(subFile)) {
                    os.write(defaultContent);
                }
            }
        } else {
            throw new IllegalStateException("Creating directory failed");
        }
    }

    private void createSubFoldersIFNeeded(String dirLoc, List<String> fileList) {
        int last_index = fileList.size() - 1;
        int index = 0;
        StringBuilder sb = new StringBuilder();
        for (String folder : fileList) {
            if (index == last_index) { // last element of the list is a file
                return;
            } else { // create folder if needed
                sb.append(File.separator).append(folder);
                File file = new File(dirLoc + sb.toString());
                if (!file.exists()) {
                    file.mkdir();
                }
            }
            index++;
        }
    }

    private void writeBytesToDir() throws IOException {
        long pieceStartIndex = this.id * this.torrent.getPieceLength();
        long currentByteIndexInDir = 0;
        int bytesWritten = 0;

        for (TorrentFile torrentFile : this.torrent.getFileList()) {
            long fileEndIndex = currentByteIndexInDir + torrentFile.getFileLength();

            if (pieceStartIndex <= currentByteIndexInDir + torrentFile.getFileLength()) {  // we have to write into that file
                String filePath = path + File.separator + String.join(File.separator, torrentFile.getFileDirs());
                long startLocation = getMinimumStartLocation(pieceStartIndex - currentByteIndexInDir);
                RandomAccessFile file = new RandomAccessFile(filePath, "rw");
                file.seek(startLocation);
                int nrOfBytesToWrite = calcBytesCount(startLocation, bytesWritten, torrent.getPieceLength(),
                        torrentFile.getFileLength());
                byte[] bytesToWrite = Arrays.copyOfRange(this.bytes, bytesWritten, bytesWritten + nrOfBytesToWrite);
                file.write(bytesToWrite);
                bytesWritten += nrOfBytesToWrite;
                file.close();

                if (bytesWritten == torrent.getPieceLength()) { // all needed bytes are written
                    return;
                }
            }
            currentByteIndexInDir = currentByteIndexInDir + torrentFile.getFileLength();
        }
    }

    private long getMinimumStartLocation(long i) {
        if (i < 0) {
            return 0;
        } else {
            return i;
        }
    }


    private int writeBytesToFile(
            TorrentFile torrentFile,
            int currentByteIndex,
            int pieceStartIndex,
            byte[] bytesToWrite
    ) throws IOException {
        String fileLocInDir = String.join(File.separator, torrentFile.getFileDirs());
        File file = new File(this.file.getPath() + File.separator + fileLocInDir);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        int nrOfOldStartBytes = Math.max(pieceStartIndex - currentByteIndex, 0);

        byte[] newContent;
        if (bytesToWrite.length + nrOfOldStartBytes <= fileContent.length) {  // can write all the bytes
            int nrOfOldEndBytes = fileContent.length - bytesToWrite.length - nrOfOldStartBytes;
            newContent = changeByteArray(fileContent, nrOfOldStartBytes, bytesToWrite, nrOfOldEndBytes);
        } else {  // all the bytes do not fit
            int nrOfBytesToWrite = fileContent.length - nrOfOldStartBytes;
            bytesToWrite = Arrays.copyOfRange(bytesToWrite, 0, nrOfBytesToWrite);
            newContent = changeByteArray(fileContent, nrOfOldStartBytes, bytesToWrite, 0);
        }
        if (newContent.length == fileContent.length) {
            try (OutputStream os = new FileOutputStream(file)) {
                os.write(newContent);
            }
        } else {
            throw new IllegalStateException("Something is wrong");
        }
        return bytesToWrite.length;
    }

    private void changeByteArray(byte[] initialContent) {
        int begin = this.id * this.torrent.getPieceLength().intValue();

        for (int i = 0; i < this.getBytes().length; i++) {
            initialContent[i + begin] = this.getBytes()[i];
        }
    }

    private byte[] changeByteArray(byte[] initialContent, int nrOfOldStartBytes, byte[] bytesToWrite, int nrOfOldEndBytes) {
        byte[] beginBytes = Arrays.copyOfRange(initialContent, 0, nrOfOldStartBytes);
        byte[] firstHalf = ArrayUtils.addAll(beginBytes, bytesToWrite);
        if (initialContent.length - nrOfOldStartBytes - bytesToWrite.length > 1) {
            byte[] endBytes = Arrays.copyOfRange(initialContent, initialContent.length - nrOfOldEndBytes, initialContent.length);
            return ArrayUtils.addAll(firstHalf, endBytes);
        } else {
            return firstHalf;
        }
    }

    /*
    private void writeBytesToDir() throws IOException {
        int pieceStartIndex = this.id * this.torrent.getPieceLength().intValue();

        int currentByteIndexInDir = 0;
        int fileEndIndex;
        byte[] bytesToWrite = getBytes();

        for (TorrentFile torrentFile : this.torrent.getFileList()) {
            fileEndIndex = currentByteIndexInDir + torrentFile.getFileLength().intValue();
            if (fileEndIndex >= pieceStartIndex) {  // we have to write into that file
                // here w have to check if we write all bytes to file or not
                int writtenBytesCount = writeBytesToFile(torrentFile, currentByteIndexInDir, pieceStartIndex, bytesToWrite);
                if (writtenBytesCount >= bytesToWrite.length) { // all needed bytes are written
                    return;
                } else {
                    bytesToWrite = Arrays.copyOfRange(bytesToWrite, writtenBytesCount, bytesToWrite.length);
                }
            }
            currentByteIndexInDir = currentByteIndexInDir + torrentFile.getFileLength().intValue();
        }
    }*/


}
