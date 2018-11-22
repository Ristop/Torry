package ut.ee.torry.client;

import be.christophedetroyer.bencoding.Utils;
import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentFile;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;


public class Piece {

    private final int id;
    private final byte[] bytes;
    private final Torrent torrent;
    private final String hash;

    public Piece(int id, Torrent torrent, byte[] bytes) {
        this.id = id;
        this.bytes = bytes;
        this.torrent = torrent;
        this.hash = torrent.getPieces().get(id);
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

    public void writeBytes(String clientPath) throws IOException {
        if (torrent.isSingleFileTorrent()) {
            try {
                writeBytesToFile(clientPath + File.separator + this.torrent.getName());
            } catch (IOException e) {
                createFile(clientPath + File.separator + this.torrent.getName());
                writeBytesToFile(clientPath + File.separator + this.torrent.getName());
            }
        } else {
            try {
                writeBytesToDir(clientPath + File.separator + this.torrent.getName());
            } catch (IOException e) {
                createDirectory(clientPath + File.separator + this.torrent.getName());
                writeBytesToDir(clientPath + File.separator + this.torrent.getName());
            }
        }
    }

    private void createFile(String path) throws IOException {
        File file = new File(path);
        byte[] defaultContent = new byte[torrent.getTotalSize().intValue()];
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(defaultContent);
        }
    }

    private void createDirectory(String dirLoc) throws IOException {
        File file = new File(dirLoc);
        boolean success = file.mkdir();
        if (success) {
            for (TorrentFile torrentFile : this.torrent.getFileList()) {
                if (torrentFile.getFileDirs().size() > 1) {
                    createSubFoldersIFNeeded(dirLoc, torrentFile.getFileDirs());
                }
                String fileLocInDir = String.join(File.separator, torrentFile.getFileDirs());
                file = new File(dirLoc + File.separator + fileLocInDir);
                byte[] defaultContent = new byte[torrentFile.getFileLength().intValue()];
                try (OutputStream os = new FileOutputStream(file)) {
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

    private void writeBytesToFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        byte[] newContent = changeByteArray(fileContent);

        try (OutputStream os = new FileOutputStream(file)) {
            os.write(newContent);
        }
    }

    private int writeBytesToFile(String dirPath, TorrentFile torrentFile, int currentByteIndex,
                                 int pieceStartIndex, byte[] bytesToWrite) throws IOException {
        String fileLocInDir = String.join(File.separator, torrentFile.getFileDirs());
        File file = new File(dirPath + File.separator + fileLocInDir);
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

    private byte[] changeByteArray(byte[] initialContent) {
        int pieceBeginningIndex = this.id * this.torrent.getPieceLength().intValue();
        int pieceEndingIndex = pieceBeginningIndex + this.bytes.length;

        byte[] beginBytes = Arrays.copyOfRange(initialContent, 0, pieceBeginningIndex);
        byte[] endBytes;
        if (this.id != torrent.getPieces().size() - 1) {
            endBytes = Arrays.copyOfRange(initialContent, pieceEndingIndex, initialContent.length);
        } else { // End piece // TODO : needs refactoring
            endBytes = new byte[0];
        }

        byte[] firstHalf = ArrayUtils.addAll(beginBytes, this.getBytes());
        return ArrayUtils.addAll(firstHalf, endBytes);
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

    private void writeBytesToDir(String dirPath) throws IOException {
        int pieceStartIndex = this.id * this.torrent.getPieceLength().intValue();

        int currentByteIndexInDir = 0;
        int fileEndIndex;
        byte[] bytesToWrite = getBytes();

        for (TorrentFile torrentFile : this.torrent.getFileList()) {
            fileEndIndex = currentByteIndexInDir + torrentFile.getFileLength().intValue();
            if (fileEndIndex >= pieceStartIndex) {  // we have to write into that file
                int writtenBytesCount = writeBytesToFile(dirPath, torrentFile, currentByteIndexInDir, pieceStartIndex,
                        bytesToWrite);  // here w have to chekc if we write all bytes to file or not
                if (writtenBytesCount >= bytesToWrite.length) { // all needed bytes are written
                    return;
                } else {
                    bytesToWrite = Arrays.copyOfRange(bytesToWrite, writtenBytesCount, bytesToWrite.length);
                }
            }
            currentByteIndexInDir = currentByteIndexInDir + torrentFile.getFileLength().intValue();
        }
    }


}
