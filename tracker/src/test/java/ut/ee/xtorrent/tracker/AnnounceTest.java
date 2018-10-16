package ut.ee.xtorrent.tracker;

import org.junit.jupiter.api.Test;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AnnounceTest {

    @Test
    public void testTwoStartEventsForSameInfoHash() {
        HttpServletRequest request1 = mock(HttpServletRequest.class);
        when(request1.getRemoteAddr()).thenAnswer(inv -> "192.0.2.1");
        TrackedTorrentService torrentService = new TrackedTorrentService();
        TrackerController controller = new TrackerController(torrentService);

        TrackerResponse res1 = announceNonOptional(
                controller,
                request1,
                "cf23df2207d99a74fbe169e3eba035e633b65d94",
                "peer1",
                6551,
                0,
                0,
                10000,
                "start"
        );

        assertEquals(0, (long) res1.getComplete());
        assertEquals(1, (long) res1.getIncomplete());
        assertEquals(0, res1.getPeers().size());

        HttpServletRequest request2 = mock(HttpServletRequest.class);
        when(request1.getRemoteAddr()).thenAnswer(inv -> "192.0.2.2");
        TrackerResponse res2 = announceNonOptional(
                controller,
                request2,
                "cf23df2207d99a74fbe169e3eba035e633b65d94",
                "peer2",
                6553,
                0,
                0,
                10000,
                "start"
        );

        assertEquals(0, (long) res2.getComplete());
        assertEquals(2, (long) res2.getIncomplete());

        Set<Peer> peers = res2.getPeers();
        assertEquals(1, peers.size());
        Peer peer = peers.stream().findFirst().get();
        assertEquals("peer1", peer.getId());
        assertEquals("192.0.2.1", peer.getIp());
        assertEquals(0, peer.getDownloaded());
        assertEquals(10000, peer.getLeft());
        assertEquals(0, peer.getUploaded());
    }

    public TrackerResponse announceNonOptional(
            TrackerController controller,
            HttpServletRequest request,
            String infoHash,
            String peerId,
            int port,
            long uploaded,
            long downloaded,
            long left,

            String event
    ) {
        return controller.announce(
                request,
                infoHash,
                peerId,
                port,
                uploaded,
                downloaded,
                left,
                false,
                null,
                event,
                null,
                null,
                null,
                null
        );
    }

}
