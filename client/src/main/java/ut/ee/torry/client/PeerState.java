package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import ut.ee.torry.common.Peer;

import java.io.IOException;

/**
 * This class represent the state of a peer that client currently knows about
 * State is updated when new events are received
 */
public class PeerState implements AutoCloseable {

    private final Peer peer;
    private volatile boolean sentHandshake = false;
    private volatile boolean receivedHandshake = false;
    private boolean[] bitField = null;

    private final NetworkManager networkManager;

    public PeerState(Peer peer) throws IOException {
        this.peer = peer;
        this.networkManager = new NetworkManager(peer);
    }

    public synchronized void setPeerHave(int index) {
        bitField[index] = true;
    }

    public void setBitField(boolean[] bitField) {
        this.bitField = bitField;
    }

    public boolean bitFieldSet() {
        return bitField != null;
    }

    public boolean hasPiece(int index) {
        return bitField[index];
    }

    public void sendHave(int index) throws IOException {
        networkManager.have(index);
    }

    public void sendBitField(boolean[] bitField) throws IOException {
        networkManager.bitField(bitField);
    }

    public void sendPiece(Piece piece) throws IOException {
        networkManager.sendPiece(piece);
    }

    public void sendRequestPiece(int index) throws IOException {
        networkManager.requestPiece(index);
    }

    public void sendHandShake(Torrent torrent, String peerId) throws IOException {
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
