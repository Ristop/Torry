package ut.ee.torry.client.event;

public class Have implements TorryRequest {

    private final String peerId;
    private final int index;

    public Have(String peerId, int index) {
        this.peerId = peerId;
        this.index = index;
    }

    public String getPeerId() {
        return peerId;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "Have{" +
                "peerId='" + peerId + '\'' +
                ", index=" + index +
                '}';
    }

}
