package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.ee.torry.client.event.SendPiece;
import ut.ee.torry.client.event.TorrentRequest;
import ut.ee.torry.common.Peer;
import ut.ee.torry.common.TrackerResponse;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DownloadTorrentTask implements Callable<DownloadTorrentTask>, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(DownloadTorrentTask.class);

    // temp
    private Random random = new Random();

    private final ScheduledExecutorService announceExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService seederExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final long DEFAULT_ANNOUNCE_INTERVAL = 15L;
    private static final long DEFAULT_SEEDING_INTERVAL = 15L;

    private final String peerId;
    private final int port;

    private final Torrent torrent;
    private final String downloadDir;
    private final PiecesHandler piecesHandler;
    private final Announcer announcer;
    private final BlockingQueue<TorrentRequest> eventQueue;
    private final Map<Peer, PeerState> peers = new ConcurrentHashMap<>();

    public DownloadTorrentTask(
            String peerId,
            int port,
            Torrent torrent,
            String downloadDir,
            Announcer announcer,
            BlockingQueue<TorrentRequest> eventQueue
    ) throws IOException {
        this.peerId = peerId;
        this.port = port;
        this.torrent = torrent;
        this.downloadDir = downloadDir;
        this.announcer = announcer;
        this.eventQueue = eventQueue;
        this.piecesHandler = new PiecesHandler(torrent, downloadDir);
        startAnnouncer();
        startSeeder();
    }

    public void announceStop() {
        log.info("Announcing stop");
        announcer.announce(
                new Announcer.AnnounceParams(
                        torrent.getAnnounce(),
                        torrent.getInfo_hash(),
                        peerId,
                        port,
                        0,
                        piecesHandler.getBytesDownloaded(),
                        torrent.getTotalSize() - piecesHandler.getBytesDownloaded()
                ).withEvent("stop")
        );
    }

    private void startAnnouncer() {
        log.info("Running announcer");
        announceExecutor.scheduleAtFixedRate(
                this::announceAndHandleResponse, 5L, DEFAULT_ANNOUNCE_INTERVAL, TimeUnit.SECONDS
        );
    }

    private void startSeeder() {
        log.info("Running seeder");
        seederExecutor.scheduleAtFixedRate(
                this::seed, 5L, DEFAULT_SEEDING_INTERVAL, TimeUnit.SECONDS
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

    private void seed() {
        List<Integer> existing = new ArrayList<>(piecesHandler.getExistingPieceIndexes());
        if (!existing.isEmpty()) {
            // Right now, just send a random piece to the first peer
            List<Map.Entry<Peer, PeerState>> entries = new ArrayList<>(peers.entrySet());
            if (!entries.isEmpty()) {
                Map.Entry<Peer, PeerState> first = entries.get(0);
                Peer peer = first.getKey();
                PeerState peerState = first.getValue();
                try {
                    int index = random.nextInt(existing.size());
                    Piece piece = piecesHandler.getPiece(existing.get(index));
                    peerState.sendPiece(piece);
                    log.info("Sent piece with index {} to peer {}.", index, peer);
                } catch (IOException e) {
                    log.error("Unable to read and send piece: ", e);
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

        while (!Thread.currentThread().isInterrupted()) {
            TorrentRequest event = eventQueue.take();
            log.info("Received event: {}", event);

            if (event instanceof SendPiece) {
                SendPiece sendPiece = (SendPiece) event;
                try {
                    if (!piecesHandler.hasPiece(sendPiece.getIndex())) {
                        log.info("Writing piece {}", sendPiece);
                        piecesHandler.writePiece(sendPiece.getIndex(), sendPiece.getBytes());
                        log.info("Successfully wrote piece {}", sendPiece);
                    }
                } catch (IOException e) {
                    log.error("Unable to write received piece: ", e);
                }
            }
            // TODO: handle other events
        }

        return this;
    }

    @Override
    public String toString() {
        return torrent.getName() + " -> " + Paths.get(downloadDir, torrent.getName());
    }

    @Override
    public void close() throws Exception {
        for (PeerState peerState : peers.values()) {
            peerState.close();
        }
    }

}
