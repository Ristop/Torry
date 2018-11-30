package ut.ee.torry.client.event;

public class SendPiece implements TorryRequest {

    private final short index;
    private final byte[] bytes;

    public SendPiece(short index, byte[] bytes) {
        this.index = index;
        this.bytes = bytes;
    }

    public short getIndex() {
        return index;
    }

    public byte[] getBytes() {
        return bytes;
    }

    @Override
    public String toString() {
        return "SendPiece{" +
                "index=" + index +
                ", bytes=<ommitted>" +
                '}';
    }

}
