package ut.ee.torry.tracker;

import java.util.Objects;

public class Peer {

    private final String id;
    private final String ip;
    private final int port;

    private long uploaded;
    private long downloaded;
    private long left;

    /**
     * @param id         unique ID for the client (peer).
     * @param ip         ip address for the client (peer)
     * @param port       port of the client
     * @param uploaded   number of bytes uploaded for given torrent
     * @param downloaded number of bytes downloaded for given torrent
     * @param left       number of bytes left to download for given torrent
     */
    public Peer(String id, String ip, int port, long uploaded, long downloaded, long left) {
        this.id = id;
        this.ip = ip;
        this.port = port;
        this.uploaded = uploaded;
        this.downloaded = downloaded;
        this.left = left;
    }

    public String getId() {
        return id;
    }

    public String getIp() {
        return ip;
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

    public boolean isComplete() {
        return left == 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Peer peer = (Peer) o;
        return Objects.equals(id, peer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Peer{" +
                "id='" + id + '\'' +
                ", port=" + port +
                '}';
    }

}
