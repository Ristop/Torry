package ut.ee.torry.client.event;

public class RequestPiece implements TorryRequest {

    private final short index;
    private final String peerId;

    public RequestPiece(short index, String peerId) {
        this.index = index;
        this.peerId = peerId;
    }

    public short getIndex() {
        return index;
    }

    public String getPeerId() {
        return peerId;
    }

    @Override
    public String toString() {
        return "RequestPiece{" +
                "index=" + index +
                '}';
    }

}
