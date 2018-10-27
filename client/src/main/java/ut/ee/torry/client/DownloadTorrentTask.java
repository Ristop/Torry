package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class DownloadTorrentTask implements Callable<DownloadTorrentTask> {

    private static final Logger log = LoggerFactory.getLogger(DownloadTorrentTask.class);

    private final Torrent torrent;
    private final String downloadDir;

    public DownloadTorrentTask(
            Torrent torrent,
            String downloadDir
    ) {
        this.torrent = torrent;
        this.downloadDir = downloadDir;
    }

    @Override
    public DownloadTorrentTask call() throws Exception {
        log.info("Starting downloading torrent: ", torrent.getName());
        return this;
    }

    @Override
    public String toString() {
        return torrent.getName() + " -> " + downloadDir;
    }

}
