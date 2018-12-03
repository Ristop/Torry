package ut.ee.torry.client.event;

public class BitField implements TorryRequest {

    private final boolean[] bitField;
    private final String peerId;

    public BitField(boolean[] bitField, String peerId) {
        this.bitField = bitField;
        this.peerId = peerId;
    }

    public boolean[] getBitField() {
        return bitField;
    }

    public String getPeerId() {
        return peerId;
    }

}
