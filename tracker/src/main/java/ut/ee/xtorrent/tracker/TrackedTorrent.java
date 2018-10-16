package ut.ee.xtorrent.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Represents a state of a tracked torrent file which include the infohash for the torrent and peers tracked for this torrent.
 */
public class TrackedTorrent {

    private static final Logger log = LoggerFactory.getLogger(TrackedTorrent.class);

    private final String infoHash;
    private final ConcurrentMap<String, Peer> peers = new ConcurrentHashMap<>();

    public TrackedTorrent(String infoHash) {
        this.infoHash = infoHash;
    }

    /**
     * @return count of the peers who have the whole file. Aka Seeders.
     */
    public long getCompleted() {
        return peers.values().stream().filter(Peer::isComplete).count();
    }

    /**
     * @return count of the peers who don't have the whole file. Aka Leechers.
     */
    public long getInComplete() {
        return peers.size() - getCompleted();
    }

    public void update(Peer peer, Event event) {
        if (event == Event.COMPLETE || peer.getLeft() == 0) {
            addPeer(peer);
        } else if (event == Event.START) {
            if (peers.containsKey(peer.getId())) {
                log.warn("Got event {}, but peer {} is already tracked for torrent {}.", event, peer, this);
            }
            addPeer(peer);
        } else if (event == Event.STOP) {
            peers.remove(peer.getId());
        } else if (event == Event.PERIODIC) {
            addPeer(peer);
        } else {
            log.warn("Unknown event {}, skipping update.", event);
        }
    }

    /**
     * @param peer helper method to add or replace the peer
     */
    private void addPeer(Peer peer) {
        peers.put(peer.getId(), peer);
    }

    /**
     * @param peer peer for which peers are requested
     * @return all peers excluding @param peer
     */
    private Set<Peer> getPeers(Peer peer) {
        HashMap<String, Peer> allPeers = new HashMap<>(peers);
        allPeers.remove(peer.getId());
        return new HashSet<>(allPeers.values());
    }

    public TrackerResponse getResponseForPeer(Peer peer, String trackerId, int interval) {
        return TrackerResponse.builder()
                .setInterval(interval)
                .setTrackerId(trackerId)
                .setComplete(getCompleted())
                .setIncomplete(getInComplete())
                .setPeers(getPeers(peer))
                .build();
    }

    @Override
    public String toString() {
        return "TrackedTorrent{" +
                "infoHash='" + infoHash + '\'' +
                '}';
    }

}
