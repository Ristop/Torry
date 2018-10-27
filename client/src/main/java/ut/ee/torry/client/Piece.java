package ut.ee.torry.client;

import be.christophedetroyer.bencoding.Utils;
import be.christophedetroyer.torrent.Torrent;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Arrays;

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
        String calculatedHash = Utils.bytesToHex(DigestUtils.sha(this.bytes));
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
            // TODO: multiple torrent file case
            throw new NotImplementedException("Multiple torrent file write bytes not implemented.");
        }
    }

    private void createFile(String path) throws IOException {
        File file = new File(path);
        byte[] defaultContent = new byte[torrent.getTotalSize().intValue()];
        try (OutputStream os = new FileOutputStream(file)) {
            os.write(defaultContent);
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

    private byte[] changeByteArray(byte[] fileContent) {
        int pieceBeginningIndex = this.id * this.torrent.getPieceLength().intValue();
        int pieceEndingIndex = pieceBeginningIndex + this.torrent.getPieceLength().intValue();

        byte[] beginBytes = Arrays.copyOfRange(fileContent, 0, pieceBeginningIndex);
        byte[] endBytes = Arrays.copyOfRange(fileContent, pieceEndingIndex, fileContent.length);
        byte[] firstHalf = ArrayUtils.addAll(beginBytes, this.getBytes());
        return ArrayUtils.addAll(firstHalf, endBytes);
    }

}
