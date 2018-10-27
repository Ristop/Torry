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
    private final boolean valid;
    private final Torrent torrent;

    public Piece(int id, Torrent torrent, byte[] bytes) {
        this.id = id;
        this.bytes = bytes;
        this.torrent = torrent;
        this.valid = verifyCorrectness();
    }

    private boolean verifyCorrectness() {
        return true;
    }

    public int getId() {
        return id;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String getHash() {
        return torrent.getPieces().get(id);
    }

    public boolean isValid() {
        byte[] digbyte = DigestUtils.sha(this.bytes);
        String sha1 = Utils.bytesToHex(digbyte);
        return getHash().equals(sha1);
    }

    public void writeBytes(String clientPath) {
        if (torrent.isSingleFileTorrent()) {
            try {
                writeBytesToFile(clientPath + File.separator + this.torrent.getName());
            } catch (IOException e) {
                createFile(clientPath + File.separator + this.torrent.getName());
                try {
                    writeBytesToFile(clientPath + File.separator + this.torrent.getName());
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } else {
            // TODO: multiple torrent file case
            throw new NotImplementedException("Multiple torrent file write bytes not implemented.");
        }
    }

    private void createFile(String path) {
        try {
            File file = new File(path);
            byte[] defaultContent = new byte[torrent.getTotalSize().intValue()];
            OutputStream os = new FileOutputStream(file);
            os.write(defaultContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeBytesToFile(String filePath) throws IOException {
        File file = new File(filePath);
        byte[] fileContent = Files.readAllBytes(file.toPath());
        byte[] newContent = changeByteArray(fileContent);

        OutputStream os = new FileOutputStream(file);
        os.write(newContent);
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
