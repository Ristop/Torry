package ut.ee.xtorrent.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


// I'm tinking that this class might be responsible for communicating with tracker and client. (one object for a torrent file)
public class TorrentHandler {

    private static final Logger log = LoggerFactory.getLogger(TorrentHandler.class);

    private final Torrent torrent;
    private final String clientPath;
    private final PiecesHandler piecesHandler;

    public TorrentHandler(Torrent torrent, String clientPath) {
        this.torrent = torrent;
        this.clientPath = clientPath;
        this.piecesHandler = new PiecesHandler(torrent, clientPath);

    }


}
