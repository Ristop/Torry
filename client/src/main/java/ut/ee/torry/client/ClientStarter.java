package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ut.ee.torry.client.event.TorrentRequest;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;

import static ut.ee.torry.client.configuration.ClientConfiguration.DOWNLOADED_FILES_DIR;
import static ut.ee.torry.client.configuration.ClientConfiguration.PEER_ID;
import static ut.ee.torry.client.configuration.ClientConfiguration.PORT;
import static ut.ee.torry.client.configuration.ClientConfiguration.TORRENT_FILES_DIR;
import static ut.ee.torry.client.util.TorrentFilesUtil.readAllTorrentFiles;

@Component
public class ClientStarter {

    private static final Logger log = LoggerFactory.getLogger(ClientStarter.class);

    private static final int DEFAULT_THREAD_POOL_SIZE = 10;

    private final ExecutorService executorService;

    private final String peerId;
    private final int port;
    private final List<Torrent> torrents;
    private final String downloadedFiledDir;
    private final Announcer announcer;

    @Autowired
    public ClientStarter(
            @Qualifier(PEER_ID) String peerId,
            @Qualifier(PORT) int port,
            @Qualifier(TORRENT_FILES_DIR) String torrentFilesDir,
            @Qualifier(DOWNLOADED_FILES_DIR) String downloadedFiledDir,
            Announcer announcer
    ) throws IOException {
        this.peerId = Objects.requireNonNull(peerId);
        this.port = port;
        this.torrents = readAllTorrentFiles(Objects.requireNonNull(torrentFilesDir));
        this.downloadedFiledDir = Objects.requireNonNull(downloadedFiledDir);
        this.announcer = announcer;
        this.executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
    }

    @PostConstruct
    private void start() throws InterruptedException, ExecutionException, IOException {
        log.info("Starting client with peer id {}, listening on port {}.", peerId, port);
        downloadTorrents();
    }

    /**
     * Entry point for starting downloads. WORK IN PROGRESS and not yet fully implemented
     */
    private void downloadTorrents() throws InterruptedException, ExecutionException, IOException {

        // Start listener
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        BlockingQueue<TorrentRequest> queue = new ArrayBlockingQueue<>(10);
        executorService.execute(new ClientServerListener(port, queue));

        CompletionService<DownloadTorrentTask> completionService = new ExecutorCompletionService<>(this.executorService);

        // Create a download torrent task for each torrent.
        // I suspect that this pattern is temporary and will change as the code progresses
        for (Torrent torrent : torrents) {
            completionService.submit(
                    new DownloadTorrentTask(peerId, port, torrent, downloadedFiledDir, announcer, queue)
            );
        }

        // Start waiting for tasks to finish
        for (int i = 0; i < torrents.size(); i++) {
            Future<DownloadTorrentTask> future = completionService.take();

            DownloadTorrentTask downloadTorrentTask = future.get();
            log.info("{} finished.", downloadTorrentTask);
        }

        this.executorService.shutdownNow();
    }

}
