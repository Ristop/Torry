package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import ut.ee.torry.common.Peer;

import java.io.IOException;

public class PeerState implements AutoCloseable {

    private boolean sentHandshake = false;
    private boolean receivedHandshake = false;

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

    public void requestPiece(int index) throws IOException {
        networkManager.requestPiece(index);
    }

    public void handShake(Torrent torrent, String peerId) throws IOException {
        networkManager.handShake(torrent, peerId);
        this.sentHandshake = true;
    }

    public boolean handshakeDone() {
        return this.sentHandshake && this.receivedHandshake;
    }

    public void recievedHandshake() {
        this.receivedHandshake = true;
    }

    @Override
    public void close() throws Exception {
        networkManager.close();
    }

}
