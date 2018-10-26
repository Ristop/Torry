package ut.ee.xtorrent.client;

import be.christophedetroyer.bencoding.Utils;
import be.christophedetroyer.torrent.Torrent;
import com.sun.deploy.util.ArrayUtil;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.util.Arrays;

public class Piece {

    private final int id;
    private final byte[] bytes;
    private final boolean isCorrect;
    private final Torrent torrent;

    public Piece(int id, Torrent torrent, byte[] bytes){
        this.id = id;
        this.bytes = bytes;
        this.torrent = torrent;
        this.isCorrect = verifyCorrectness();
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

    public boolean isCorrect() {
        byte[] digbyte = DigestUtils.sha(this.bytes);
        String sha1 = Utils.bytesToHex(digbyte);
        return getHash().equals(sha1);
    }

    public void writeBytes(String clientPath) {                                   //todo multiple torrent file case
        if (torrent.isSingleFileTorrent())
            writeBytesToFile(clientPath + "/" + this.torrent.getName());
    }

    private void writeBytesToFile(String filePath) {
        File file = new File(filePath);
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            System.out.println(fileContent.length);
            byte[] newContent = changeByteArray(fileContent);
            System.out.println(newContent.length);

            OutputStream os = new FileOutputStream(file);
            os.write(newContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] changeByteArray(byte[] fileContent) {
        int pieceBeginningIndex = this.id * this.torrent.getPieceLength().intValue();
        int pieceEndingIndex = pieceBeginningIndex + this.torrent.getPieceLength().intValue();

        byte[] beginBytes = Arrays.copyOfRange(fileContent, 0, pieceBeginningIndex);
        byte[] endBytes = Arrays.copyOfRange(fileContent, pieceEndingIndex + 1, fileContent.length);
        byte[] firstHalf = concatenate(beginBytes, this.getBytes());
        return  concatenate(firstHalf, endBytes);
    }

    // taken from https://stackoverflow.com/questions/80476/how-can-i-concatenate-two-arrays-in-java
    private byte[] concatenate(byte[] a, byte[] b) {
        int aLen = a.length;
        int bLen = b.length;

        @SuppressWarnings("unchecked")
        byte[] c = (byte[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);

        return c;
    }
}
