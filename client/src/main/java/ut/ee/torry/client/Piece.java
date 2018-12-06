package ut.ee.torry.client;

import be.christophedetroyer.bencoding.Utils;
import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentFile;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
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
        RandomAccessFile f = new RandomAccessFile(this.path, "rw");
        f.setLength(torrent.getTotalSize());
        f.close();
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
                if (torrentFile.getFileDirs().size() >= 1) {
                    createSubFoldersIFNeeded(this.file.getPath(), torrentFile.getFileDirs());
                }
                String fileLocInDir = String.join(File.separator, torrentFile.getFileDirs());
                RandomAccessFile subFile = new RandomAccessFile(this.file.getPath() + File.separator + fileLocInDir,
                        "rw");
                subFile.setLength(torrentFile.getFileLength());
                subFile.close();
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
}