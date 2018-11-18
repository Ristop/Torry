package ut.ee.torry.client;

import ut.ee.torry.common.Peer;

import java.io.IOException;

public class PeerState implements AutoCloseable {

    private boolean amChoking = true;
    private boolean interested = false;
    private boolean peerChoking = true;
    private boolean peerInterested = false;

    private final NetworkManager networkManager;

    public PeerState(Peer peer) throws IOException {
        this.networkManager = new NetworkManager(peer);
    }

    public void sendPiece(Piece piece) throws IOException {
        networkManager.sendPiece(piece);
    }

    @Override
    public void close() throws Exception {
        networkManager.close();
    }

}
