package ut.ee.xtorrent.client;

import be.christophedetroyer.bencoding.Utils;
import be.christophedetroyer.torrent.Torrent;
import org.apache.commons.codec.digest.DigestUtils;

public class Piece {

    private final int id;
    private final byte[] bytes;
    private final String hash;
    private final boolean isCorrect;

    public Piece(int id, Torrent torrent, byte[] bytes){
        this.id = id;
        this.bytes = bytes;
        this.hash = torrent.getPieces().get(id);
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
        return hash;
    }

    public boolean isCorrect() {
        byte[] digbyte = DigestUtils.sha(this.bytes);
        String sha1 = Utils.bytesToHex(digbyte);
        return this.hash.equals(sha1);
    }
}
