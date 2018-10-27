package ut.ee.torry.tracker;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class TrackedTorrentService {

    private static ConcurrentMap<String, TrackedTorrent> trackedTorrents = new ConcurrentHashMap<>();

    /**
     * @param infoHash hash value of the info key for a given torrent.
     * @return already tracked torrent if exists or initializes new tracked torrent if not
     */
    public TrackedTorrent createIfAbsentAndGet(String infoHash) {
        TrackedTorrent trackedTorrent = trackedTorrents.putIfAbsent(infoHash, new TrackedTorrent(infoHash));
        return trackedTorrent != null ? trackedTorrent : trackedTorrents.get(infoHash);
    }

}
