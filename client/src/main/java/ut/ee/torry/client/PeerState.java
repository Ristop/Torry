package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import ut.ee.torry.common.Peer;

import java.io.IOException;

public class PeerState implements AutoCloseable {

    private final Peer peer;
    private volatile boolean sentHandshake = false;
    private volatile boolean receivedHandshake = false;

    private final NetworkManager networkManager;

    public PeerState(Peer peer) throws IOException {
        this.peer = peer;
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

    public void receivedHandshake() {
        this.receivedHandshake = true;
    }

    public Peer getPeer() {
        return peer;
    }

    @Override
    public void close() throws Exception {
        networkManager.close();
    }

}
