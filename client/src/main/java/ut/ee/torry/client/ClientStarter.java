package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.PostConstruct;

import static ut.ee.torry.client.ClientConfiguration.DOWNLOADED_FILES_DIR;
import static ut.ee.torry.client.ClientConfiguration.PEER_ID;
import static ut.ee.torry.client.ClientConfiguration.PORT;
import static ut.ee.torry.client.ClientConfiguration.TORRENT_FILES_DIR;
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

    public ClientStarter(
            @Qualifier(PEER_ID) String peerId,
            @Qualifier(PORT) int port,
            @Qualifier(TORRENT_FILES_DIR) String torrentFilesDir,
            @Qualifier(DOWNLOADED_FILES_DIR) String downloadedFiledDir
    ) throws IOException {
        this.peerId = Objects.requireNonNull(peerId);
        this.port = port;
        this.torrents = readAllTorrentFiles(Objects.requireNonNull(torrentFilesDir));
        this.downloadedFiledDir = Objects.requireNonNull(downloadedFiledDir);
        this.executorService = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
    }

    @PostConstruct
    private void start() throws InterruptedException, ExecutionException {
        log.info("Starting client with peer id {}, running on port {}.", peerId, port);
        downloadTorrents();
    }

    private void downloadTorrents() throws InterruptedException, ExecutionException {
        CompletionService<DownloadTorrentTask> completionService = new ExecutorCompletionService<>(executorService);

        for (Torrent torrent : torrents) {
            completionService.submit(new DownloadTorrentTask(torrent, downloadedFiledDir));
        }

        for (int i = 0; i < torrents.size(); i++) {
            Future<DownloadTorrentTask> future = completionService.take();

            DownloadTorrentTask downloadTorrentTask = future.get();
            log.info("{} finished.", downloadTorrentTask);
        }

        executorService.shutdownNow();
    }

}
