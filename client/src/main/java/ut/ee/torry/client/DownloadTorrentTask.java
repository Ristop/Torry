package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.ee.torry.common.Peer;
import ut.ee.torry.common.TrackerResponse;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DownloadTorrentTask implements Callable<DownloadTorrentTask> {

    private static final Logger log = LoggerFactory.getLogger(DownloadTorrentTask.class);

    private final ScheduledExecutorService announceExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final long DEFAULT_ANNOUNCE_INTERVAL = 15L;

    private final String peerId;
    private final int port;

    private final Torrent torrent;
    private final String downloadDir;
    private final PiecesHandler piecesHandler;
    private final Announcer announcer;
    private final ClientServerListener csl;
    private final Map<Peer, PeerState> peers = new ConcurrentHashMap<>();

    public DownloadTorrentTask(
            String peerId,
            int port,
            Torrent torrent,
            String downloadDir,
            Announcer announcer,
            ClientServerListener csl
    ) throws IOException {
        this.peerId = peerId;
        this.port = port;
        this.torrent = torrent;
        this.downloadDir = downloadDir;
        this.announcer = announcer;
        this.csl = csl;
        this.piecesHandler = new PiecesHandler(torrent, downloadDir);
        startAnnouncer();
    }

    private void startAnnouncer() {
        announceExecutor.scheduleAtFixedRate(
                this::announceAndHandleResponse, 5L, DEFAULT_ANNOUNCE_INTERVAL, TimeUnit.SECONDS
        );
    }

    private void announceAndHandleResponse() {
        log.info("Announcing {}", torrent.getName());
        TrackerResponse response = announcer.announce(
                new Announcer.AnnounceParams(
                        torrent.getAnnounce(),
                        torrent.getInfo_hash(),
                        peerId,
                        port,
                        0,
                        piecesHandler.getBytesDownloaded(),
                        torrent.getTotalSize() - piecesHandler.getBytesDownloaded()
                )
        );

        log.info("Announce response: {}", response);

        // Save peers from response
        for (Peer peer : response.getPeers()) {
            if (!peers.containsKey(peer)) {
                try {
                    PeerState peerState = new PeerState(peer);
                    peers.put(peer, peerState);
                } catch (IOException e) {
                    log.error("Unable to add tracked peer: ", e);
                }
            }
        }
    }


    @Override
    public DownloadTorrentTask call() throws InterruptedException {
        log.info("Starting downloading torrent: {}", torrent.getName());
        log.info("Existing pieces: {}", piecesHandler.getExistingPieceIndexes());
        log.info("Not existing pieces: {}", piecesHandler.getNotExistingPieceIndexes());
        log.info("Torrent pieces count: {}", torrent.getPieces().size());

        while (piecesHandler.getNotExistingPieceIndexes().isEmpty()) {
            // TODO
            Thread.sleep(2000);
        }

        return this;
    }

    @Override
    public String toString() {
        return torrent.getName() + " -> " + Paths.get(downloadDir, torrent.getName());
    }

}
