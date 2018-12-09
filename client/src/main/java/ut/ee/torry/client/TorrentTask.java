package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import performance_testing.PerformanceTest;
import ut.ee.torry.client.event.RequestPiece;
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
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class TorrentTask implements Callable<TorrentTask>, AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(TorrentTask.class);

    // temp
    private Random random = new Random();

    private final ScheduledExecutorService announceExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService seederExecutor = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService requesterExecutor = Executors.newSingleThreadScheduledExecutor();
    private static final long DEFAULT_ANNOUNCE_INTERVAL = 10L;
    private static final long DEFAULT_REQUEST_INTERVAL = 200L;

    private final String peerId;
    private final int port;

    private final Torrent torrent;
    private final String downloadDir;
    private final PiecesHandler piecesHandler;
    private final Announcer announcer;
    private final BlockingQueue<TorrentRequest> eventQueue;
    private final BlockingQueue<RequestPiece> seedQueue;
    private final Map<Peer, PeerState> peers = new ConcurrentHashMap<>();

    private boolean keepGoing;

    private final PerformanceTest performanceTest;

    public TorrentTask(
            String peerId,
            int port,
            Torrent torrent,
            String downloadDir,
            Announcer announcer,
            BlockingQueue<TorrentRequest> eventQueue
    ) throws IOException {

        this.keepGoing = true;
        this.peerId = peerId;
        this.port = port;
        this.torrent = torrent;
        this.downloadDir = downloadDir;
        this.announcer = announcer;
        this.eventQueue = eventQueue;
        this.piecesHandler = new PiecesHandler(torrent, downloadDir);
        this.performanceTest = new PerformanceTest("./client/src/main/java/performance_testing/times_for_10MB.txt",piecesHandler.getFilePath());
        seedQueue = new ArrayBlockingQueue<>(32);
        startAnnouncer();
        startSeeder();
        startRequester();

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
        seederExecutor.execute(() -> {
            try {
                seed();
            } catch (InterruptedException e) {
                log.error("Unable to seed: ", e);
            }
        });
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

    private void seed() throws InterruptedException {
        while (!Thread.currentThread().isInterrupted()) {
            RequestPiece request = seedQueue.take();

            if (piecesHandler.hasPiece(request.getIndex())) {
                // Right now, just send a piece to the first peer
                List<Map.Entry<Peer, PeerState>> entries = new ArrayList<>(peers.entrySet());
                if (!entries.isEmpty()) {
                    Map.Entry<Peer, PeerState> first = entries.get(0);
                    Peer peer = first.getKey();
                    try {
                        Piece piece = piecesHandler.getPiece(request.getIndex());
                        sendPiece(peer, piece);
                        log.info("Sent piece with index {} to peer {}.", request.getIndex(), peer);
                    } catch (IOException e) {
                        peers.remove(peer);
                        log.error("Failed to seed to peer {}. Closing connection:", peer, e);
                    }
                }
            } else {
                log.warn("Received request for piece with index {} but client does not have it.", request.getIndex());
            }
        }
    }

    private void sendPiece(Peer peer, Piece piece) throws IOException {
        peers.get(peer).sendPiece(piece);
    }

    private void request() {
    //    performanceTest.startClockIfNotStarted();

        List<Integer> existing = new ArrayList<>(piecesHandler.getNotExistingPieceIndexes());

        if(existing.isEmpty()){
            performanceTest.stopClock();
            performanceTest.writeTimeToFile();
            log.info("Performance tesing done for file: {}", piecesHandler.getFilePath());
            performanceTest.deleteFile();
            requesterExecutor.shutdown();
            this.keepGoing = false;
        }

        int index = existing.get(random.nextInt(existing.size()));


        List<Map.Entry<Peer, PeerState>> entries = new ArrayList<>(peers.entrySet());

        if (!entries.isEmpty()) {
            Map.Entry<Peer, PeerState> first = entries.get(0);
            Peer peer = first.getKey();
            PeerState peerState = first.getValue();
            try {
                peerState.requestPiece(index);
                log.info("Sent piece request for piece with index {} to peer {}.", index, peer);
            } catch (IOException e) {
                peers.remove(peer);
                log.error("Failed to seed to peer {}. Closing connection:", peer, e);
            }
        }

    }

    @Override
    public TorrentTask call() throws InterruptedException {
        log.info("Starting torrent task for {}", torrent.getName());
        log.info("Existing pieces: {}", piecesHandler.getExistingPieceIndexes());
        log.info("Not existing pieces: {}", piecesHandler.getNotExistingPieceIndexes());
        log.info("Torrent pieces count: {}", torrent.getPieces().size());


        while (!Thread.currentThread().isInterrupted()) {



  //          TorrentRequest event = eventQueue.take();
            TorrentRequest event;
              do{
                  event = eventQueue.poll(20, TimeUnit.SECONDS);

              }while(keepGoing && event == null);


            if(!keepGoing && event == null){
                try {
                    close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                announceExecutor.shutdown();
                seederExecutor.shutdown();
                return this;
            }

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
            } else if (event instanceof RequestPiece) {
                RequestPiece reqEvent = (RequestPiece) event;
                if (!seedQueue.contains(reqEvent)) {
                    seedQueue.put(reqEvent);
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
