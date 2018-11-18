package ut.ee.torry.client;

import ut.ee.torry.common.Peer;

import java.io.IOException;

public class PeerState {

    private boolean amChoking = true;
    private boolean interested = false;
    private boolean peerChoking = true;
    private boolean peerInterested = false;

    private final NetworkManager networkManager;

    public PeerState(Peer peer) throws IOException {
        this.networkManager = new NetworkManager(peer);
    }

}
