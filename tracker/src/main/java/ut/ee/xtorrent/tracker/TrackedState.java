package ut.ee.xtorrent.tracker;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TrackedState {

    private final String infoHash;
    private final HashMap<String, Peer> peers = new HashMap<>();

    public TrackedState(String infoHash) {
        this.infoHash = infoHash;
    }

    public long getCompleted() {
        return peers.values().stream().filter(Peer::isComplete).count();
    }

    public long getInComplete() {
        return peers.size() - getCompleted();
    }

    public Set<Peer> getPeers(String peerId) {
        HashMap<String, Peer> peersExcluded = new HashMap<>(peers);
        peersExcluded.remove(peerId);
        return new HashSet<>(peersExcluded.values());
    }

    public void addPeer(Peer peer) {
        if (!peers.containsKey(peer.getPeerId())) {
            peers.put(peer.getPeerId(), peer);
        }
    }

    public void setPeerComplete(String peerId) {
        peers.get(peerId).setLeft(0);
    }

    public void removePeer(String peerId) {
        peers.remove(peerId);
    }

    public void updatePeer(String peerId, long uploaded, long downloaded, long left) {
        Peer peer = peers.get(peerId);
        peer.setUploaded(uploaded);
        peer.setDownloaded(uploaded);
        peer.setLeft(uploaded);
    }

}
