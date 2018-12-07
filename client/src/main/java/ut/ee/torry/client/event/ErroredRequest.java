package ut.ee.torry.client.event;

public class ErroredRequest implements TorryRequest {

    private final String peerId;

    public ErroredRequest(String peerId) {
        this.peerId = peerId;
    }

    public String getPeerId() {
        return peerId;
    }

    @Override
    public String toString() {
        return "ErroredRequest{" +
                "peerId='" + peerId + '\'' +
                '}';
    }

}
