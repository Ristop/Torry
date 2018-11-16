package ut.ee.torry.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class TrackedTorrentService {

    private static final Logger log = LoggerFactory.getLogger(TrackedTorrentService.class);

    private ConcurrentMap<String, TrackedTorrent> trackedTorrents = new ConcurrentHashMap<>();

    /**
     * @param infoHash hash value of the info key for a given torrent.
     * @return already tracked torrent if exists or initializes new tracked torrent if not
     */
    public TrackedTorrent createIfAbsentAndGet(String infoHash) {
        TrackedTorrent trackedTorrent = trackedTorrents.putIfAbsent(infoHash, new TrackedTorrent(infoHash));
        if (trackedTorrent != null) {
            return trackedTorrent;
        } else {
            log.info("Adding new tracked torrent with info hash: {}", infoHash);
            return trackedTorrents.get(infoHash);
        }
    }

    public ConcurrentMap<String, TrackedTorrent> getTrackedTorrents() {
        return trackedTorrents;
    }

}
