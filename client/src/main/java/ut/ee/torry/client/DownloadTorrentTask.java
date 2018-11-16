package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.ee.torry.common.Peer;
import ut.ee.torry.common.TrackerResponse;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
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
    private final Map<Peer, List<Integer>> peers = new ConcurrentHashMap<>();

    public DownloadTorrentTask(
            String peerId,
            int port,
            Torrent torrent,
            String downloadDir,
            Announcer announcer
    ) throws IOException {
        this.peerId = peerId;
        this.port = port;
        this.torrent = torrent;
        this.downloadDir = downloadDir;
        this.announcer = announcer;
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

        for (Peer peer : response.getPeers()) {
            try {
                NetworkManager nm = new NetworkManager(peer);
                for (Integer i : piecesHandler.getExistingPieceIndexes()) {
                    log.info("Sending bytes for piece with index {} to peer {}", i, peer);
                    nm.sendPiece(piecesHandler.getPiece(i));
                    break;
                }
            } catch (IOException e) {
                log.error("Error sending bytes. ", e);
            }
        }

        // TODO: handle response
    }


    @Override
    public DownloadTorrentTask call() throws IOException {
        log.info("Starting downloading torrent: {}", torrent.getName());
        log.info("Existing pieces: {}", piecesHandler.getExistingPieceIndexes());
        log.info("Not existing pieces: {}", piecesHandler.getNotExistingPieceIndexes());
        log.info("Torrent pieces count: {}", torrent.getPieces().size());


        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ExecutorService executor = Executors.newCachedThreadPool();

            while (true) {
                Socket socket = serverSocket.accept();
                log.info("Starting to receive info");

                executor.execute(() -> {
                    try (DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
                        parseReceivedStream(in);
                    } catch (IOException e) {
                        log.error("Unable to read from socket: ", e);
                    }

                });

            }

        }

    }

    private void parseReceivedStream(DataInputStream dis) throws IOException {
        log.info("Reading info from stream");

        int len = dis.readInt();
        byte id = dis.readByte();

        switch (id) {
            case 7:
                short index = dis.readShort();
                byte[] bytes = new byte[len - 7];
                log.info("Received piece <len:{}><id:{}><index:{}><data:omitted>", len, id, index);
                break;
            default:
                log.warn("Received event with id {} which is not yet supported or unknown.", id);
        }

    }

    @Override
    public String toString() {
        return torrent.getName() + " -> " + Paths.get(downloadDir, torrent.getName());
    }

}
