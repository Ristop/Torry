package ut.ee.torry.client.event;

public class Handshake implements TorryRequest {

    private final String peerId;
    private final String torrentHash;
    private final String pstr;

    public Handshake(String peerId, String torrentHash, String pstr) {
        this.peerId = peerId;
        this.torrentHash = torrentHash;
        this.pstr = pstr;
    }

    public String getPeerId() {
        return peerId;
    }

    public String getTorrentHash() {
        return torrentHash;
    }

    public String getPstr() {
        return pstr;
    }

    @Override
    public String toString() {
        return "Handshake{" +
                "peerId='" + peerId + '\'' +
                ", torrentHash='" + torrentHash + '\'' +
                ", pstr='" + pstr + '\'' +
                '}';
    }

}
