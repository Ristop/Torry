package ut.ee.xtorrent.common;

public class Peer {

    private final String peerId;
    private final String ip;
    private final int port;

    public Peer(String peerId, String ip, int port) {
        this.peerId = peerId;
        this.ip = ip;
        this.port = port;
    }

    public String getPeerId() {
        return peerId;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

}
