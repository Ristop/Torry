package ut.ee.torry.client.event;

public class RequestPiece implements TorryRequest {

    private final short index;

    public RequestPiece(short index) {
        this.index = index;
    }

    public short getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return "RequestPiece{" +
                "index=" + index +
                '}';
    }

}
