package ut.ee.torry.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tracker")
public class TrackerController {

    private static final Logger log = LoggerFactory.getLogger(TrackerController.class);

    private static final String TRACKER_ID = "1";
    private static final int DEFAULT_INTERVAL = 60;

    private final TrackedTorrentService torrentService;

    @Autowired
    public TrackerController(TrackedTorrentService torrentService) {
        this.torrentService = torrentService;
    }

    /**
     * @param infoHash   urlencoded 20-byte SHA1 hash of the value of the info key from the Metainfo file.
     * @param peerId     urlencoded 20-byte string used as a unique ID for the client, generated by the client at startup.
     * @param port       The port number that the client is listening on.
     * @param uploaded   The total amount uploaded (since the client sent the 'started' event to the tracker) in base ten ASCII.
     * @param downloaded The total amount downloaded (since the client sent the 'started' event to the tracker) in base ten ASCII.
     * @param left       The number of bytes this client still has to download in base ten ASCII.
     * @param compact    Setting this to 1 indicates that the client accepts a compact response.
     * @param noPeerId   Indicates that the tracker can omit peer id field in peers dictionary.
     *                   This option is ignored if compact is enabled.
     * @param eventName  (Optional) If specified, must be one of started, completed, stopped.
     *                   If not specified, then this request is one performed at regular intervals.
     * @param ip         (Optional) The true IP address of the client machine,
     *                   in dotted quad format or rfc3513 defined hexed IPv6 address.
     * @param numwant    (Optional) Number of peers that the client would like to receive from the tracker.
     * @param key        (Optional) An additional identification that is not shared with any other peers.
     * @param trackerId  (Optional) If a previous announce contained a tracker id, it should be set here.
     * @return
     */
    @RequestMapping("/announce")
    public TrackerResponse announce(
            HttpServletRequest request,
            @RequestParam(value = "info_hash") String infoHash,
            @RequestParam(value = "peer_id") String peerId,
            @RequestParam(value = "port") int port,
            @RequestParam(value = "uploaded") long uploaded,
            @RequestParam(value = "downloaded") long downloaded,
            @RequestParam(value = "left") long left,
            @RequestParam(value = "compact", defaultValue = "false") boolean compact,
            @RequestParam(value = "no_peer_id", required = false) String noPeerId,
            @RequestParam(value = "event", required = false) String eventName,
            @RequestParam(value = "ip", required = false) String ip,
            @RequestParam(value = "numwant", required = false) String numwant,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "tracker_id", required = false) String trackerId
    ) {
        TrackedTorrent trackedTorrent = torrentService.createIfAbsentAndGet(infoHash);
        Peer peer = new Peer(peerId, request.getRemoteAddr(), port, uploaded, downloaded, left);
        Event event = eventName != null ? Event.getEvent(eventName) : Event.PERIODIC;
        trackedTorrent.update(peer, event);
        return trackedTorrent.getResponseForPeer(peer, TRACKER_ID, DEFAULT_INTERVAL);
    }

}