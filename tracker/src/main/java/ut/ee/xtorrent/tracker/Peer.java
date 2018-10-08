package ut.ee.xtorrent.tracker;

import java.util.Objects;

public class Peer {

    private final String peerId;
    private final int port;

    private long uploaded;
    private long downloaded;
    private long left;

    public Peer(String peerId, int port, long left) {
        this.peerId = peerId;
        this.port = port;
        this.left = left;
    }

    public String getPeerId() {
        return peerId;
    }

    public int getPort() {
        return port;
    }

    public long getUploaded() {
        return uploaded;
    }

    public long getDownloaded() {
        return downloaded;
    }

    public long getLeft() {
        return left;
    }

    public void setUploaded(long uploaded) {
        this.uploaded = uploaded;
    }

    public void setDownloaded(long downloaded) {
        this.downloaded = downloaded;
    }

    public void setLeft(long left) {
        this.left = left;
    }

    public boolean isComplete() {
        return left == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return Objects.equals(peerId, peer.peerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(peerId);
    }

}
