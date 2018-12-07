package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.ee.torry.client.event.BitField;
import ut.ee.torry.client.event.Handshake;
import ut.ee.torry.client.event.Have;
import ut.ee.torry.client.event.RequestPiece;
import ut.ee.torry.client.event.SendPiece;
import ut.ee.torry.client.event.TorryRequest;
import ut.ee.torry.common.Peer;
import ut.ee.torry.common.TrackerResponse;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TorrentTask implements Callable<TorrentTask>, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(TorrentTask.class);

    private Random random = new Random();

    private final ScheduledExecutorService announceExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService seederExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService requesterExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService requestedPiecesExecutor = Executors.newSingleThreadScheduledExecutor();

    private static final long DEFAULT_ANNOUNCE_INTERVAL = 10L;
    private static final long DEFAULT_REQUESTED_PIECES_CLEANING_INTERVAL = 10L;
    private static final long DEFAULT_REQUEST_INTERVAL = 10L;

    private final String peerId;
    private final int port;
    private long uploaded = 0;

    private final Torrent torrent;
    private final String downloadDir;
    private final PiecesHandler piecesHandler;
    private final Announcer announcer;
    private final BlockingQueue<TorryRequest> eventQueue;
    private final BlockingQueue<RequestPiece> seedQueue;
    private final Map<String, PeerState> peers = new ConcurrentHashMap<>();
    private final Set<String> queuedHandshakes = new HashSet<>();
    private final Set<Integer> requestedPieces = ConcurrentHashMap.newKeySet();

    public TorrentTask(
            String peerId,
            int port,
            Torrent torrent,
            String downloadDir,
            Announcer announcer,
            BlockingQueue<TorryRequest> eventQueue
    ) throws IOException {
        this.peerId = peerId;
        this.port = port;
        this.torrent = torrent;
        this.downloadDir = downloadDir;
        this.announcer = announcer;
        this.eventQueue = eventQueue;
        this.piecesHandler = new PiecesHandler(torrent, downloadDir);
        seedQueue = new ArrayBlockingQueue<>(32);
        startAnnouncer();
        startSeeder();
        startRequester();
        startRequestedPiecesCleaner();
    }

    public void announceStop() {
        log.info("Announcing stop");
        announcer.announce(
                new Announcer.AnnounceParams(
                        torrent.getAnnounce(),
                        torrent.getInfo_hash(),
                        peerId,
                        port,
                        this.uploaded,
                        piecesHandler.getBytesDownloaded(),
                        piecesHandler.getTotalSize() - piecesHandler.getBytesDownloaded()
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
        seederExecutor.execute(() -> {
            try {
                seed();
            } catch (InterruptedException e) {
                log.error("Unable to seed: ", e);
            }
        });
    }

    /**
     * We keep a 10 second queue of requested pieces.
     * If we don't receive it that time range, queue will be cleared and piece will be requested again.
     */
    private void startRequestedPiecesCleaner() {
        log.info("Running requested pieces cleaner");
        requestedPiecesExecutor.scheduleAtFixedRate(
                requestedPieces::clear,
                DEFAULT_REQUESTED_PIECES_CLEANING_INTERVAL,
                DEFAULT_REQUESTED_PIECES_CLEANING_INTERVAL,
                TimeUnit.SECONDS
        );
    }

    private void startRequester() {
        log.info("Running piece requester");
        requesterExecutor.scheduleAtFixedRate(
                this::request, 5L, DEFAULT_REQUEST_INTERVAL, TimeUnit.MILLISECONDS
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
                        this.uploaded,
                        piecesHandler.getBytesDownloaded(),
                        piecesHandler.getTotalSize() - piecesHandler.getBytesDownloaded()
                )
        );

        log.info("Announce response: {}", response);

        // Save peers from response and send handshake
        for (Peer peer : response.getPeers()) {
            synchronized (peers) {
                // Process only if we already don't know this peer
                if (!peers.containsKey(peer.getId())) {
                    try {
                        PeerState peerState = new PeerState(peer);
                        peerState.sendHandShake(torrent, peerId);
                        // If handshake was already sent by the peer
                        if (queuedHandshakes.contains(peer.getId())) {
                            peerState.receivedHandshake();
                            queuedHandshakes.remove(peer.getId());
                            peerState.sendBitField(piecesHandler.getBitField());
                        }
                        peers.put(peer.getId(), peerState);
                    } catch (IOException e) {
                        log.error("Unable to add tracked peer: ", e);
                    }
                }
            }
        }
    }

    private void seed() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            // Take the request from the queue
            RequestPiece request = seedQueue.take();

            // Do we even have the piece?
            if (piecesHandler.hasPiece(request.getIndex())) {
                // Do we know about the peer that wants the piece?
                if (peers.containsKey(request.getPeerId())) {
                    PeerState peerState = peers.get(request.getPeerId());
                    // Is the handshake done?
                    if (peerState.handshakeDone() && peerState.bitFieldSet()) {
                        Peer peer = peerState.getPeer();
                        try {
                            Piece piece = piecesHandler.getPiece(request.getIndex());
                            peerState.sendPiece(piece);
                            uploaded += piece.getBytes().length;
                            log.info("Sent piece with index {} to peer {}.", request.getIndex(), peer);
                        } catch (IOException e) {
                            peers.remove(peer.getId());
                            log.error("Failed to seed to peer {}. Closing connection:", peer, e);
                        }
                    }
                } else {
                    log.warn("Received request for piece, but peer {} is unknown.", request.getPeerId());
                }
            } else {
                log.warn("Received request for piece with index {} but client does not have it.", request.getIndex());
            }
        }
    }

    private void sendHaveToAllPeers(int index) throws IOException {
        // Send have to all peers
        for (PeerState peerState : peers.values()) {
            // Only send if handshake is established and BitField is set
            if (peerState.handshakeDone() && peerState.bitFieldSet()) {
                peerState.sendHave(index);
            }
        }
    }

    private void request() {
        List<Integer> existing = new ArrayList<>(piecesHandler.getNotExistingPieceIndexes());
        // Request a random not existing piece
        int index = existing.get(random.nextInt(existing.size()));

        ArrayList<PeerState> peerStates = new ArrayList<>(peers.values());

        // Request from all tracked peers
        for (PeerState peerState : peerStates) {
            // Request only if we haven't asked it in the last 10 seconds
            if (!requestedPieces.contains(index)) {
                // Request only if:
                //  1. Handshake is established
                //  2. BitField is set
                //  3. Peer has that the requested piece
                if (peerState.handshakeDone() && peerState.bitFieldSet() && peerState.hasPiece(index)) {
                    Peer peer = peerState.getPeer();
                    try {
                        peerState.sendRequestPiece(index);
                        requestedPieces.add(index);
                        log.info("Sent piece request for piece with index {} to peer {}.", index, peer);
                    } catch (IOException e) {
                        peers.remove(peer.getId());
                        log.error("Failed to seed to peer {}. Closing connection:", peer, e);
                    }
                }
            }
        }

    }

    @Override
    public TorrentTask call() throws InterruptedException, IOException {
        log.info("Starting torrent task for {}", torrent.getName());
        log.info("Existing pieces: {}", piecesHandler.getExistingPieceIndexes());
        log.info("Not existing pieces: {}", piecesHandler.getNotExistingPieceIndexes());
        log.info("Torrent pieces count: {}", torrent.getPieces().size());

        while (!Thread.currentThread().isInterrupted()) {
            TorryRequest event = eventQueue.take();
            log.info("Received event: {}", event);

            if (event instanceof SendPiece) {
                SendPiece sendPiece = (SendPiece) event;
                try {
                    if (!piecesHandler.hasPiece(sendPiece.getIndex())) {
                        log.info("Writing piece {}", sendPiece);
                        piecesHandler.writePiece(sendPiece.getIndex(), sendPiece.getBytes());
                        sendHaveToAllPeers(sendPiece.getIndex());
                        log.info("Successfully wrote piece {}", sendPiece);
                    }
                } catch (IOException e) {
                    log.error("Unable to write received piece: ", e);
                }
            } else if (event instanceof RequestPiece) {
                RequestPiece reqEvent = (RequestPiece) event;
                if (!seedQueue.contains(reqEvent)) {
                    seedQueue.put(reqEvent);
                }
            } else if (event instanceof Handshake) {
                Handshake handshake = (Handshake) event;
                synchronized (peers) {
                    if (peers.containsKey(handshake.getPeerId())) {
                        PeerState peerState = peers.get(handshake.getPeerId());
                        peerState.receivedHandshake();
                        peerState.sendBitField(piecesHandler.getBitField());
                    } else {
                        queuedHandshakes.add(handshake.getPeerId());
                    }
                }
            } else if (event instanceof BitField) {
                BitField bitField = (BitField) event;
                synchronized (peers) {
                    peers.get(bitField.getPeerId()).setBitField(bitField.getBitField());
                }
            } else if (event instanceof Have) {
                Have have = (Have) event;
                synchronized (peers) {
                    peers.get(have.getPeerId()).setPeerHave(have.getIndex());
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
        announceStop();
        for (PeerState peerState : peers.values()) {
            peerState.close();
        }
    }

}
