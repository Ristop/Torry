package ut.ee.xtorrent.tracker;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ut.ee.xtorrent.common.Peer;
import ut.ee.xtorrent.common.TrackerResponse;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/tracker")
public class TrackerController {

    private static final String TRACKER_ID = "1";
    private static final int DEFAULT_INTERVAL = 60;

    @RequestMapping("/announce")
    public TrackerResponse announce(
            @RequestParam(value = "info_hash") String infoHash,
            @RequestParam(value = "peer_id") String peerId,
            @RequestParam(value = "port") int port,
            @RequestParam(value = "uploaded") String uploaded,
            @RequestParam(value = "downloaded") String downloaded,
            @RequestParam(value = "left") String left,
            @RequestParam(value = "compact", defaultValue = "false") boolean compact,
            @RequestParam(value = "no_peer_id") String noPeerId,
            @RequestParam(value = "event") String event,
            @RequestParam(value = "ip", required = false) String ip,
            @RequestParam(value = "numwant", required = false) String numwant,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "tracker_id", required = false) String trackerId
    ) {

        // number of peers with the entire file (seeders)
        int complete = 0;

        // number of non-seeder peers (leechers)
        int incomplete = 0;

        List<Peer> peers = new ArrayList<>();

        return TrackerResponse.builder()
                .setInterval(DEFAULT_INTERVAL)
                .setTrackerId(TRACKER_ID)
                .setComplete(complete)
                .setIncomplete(incomplete)
                .setPeers(peers)
                .build();
    }

}
