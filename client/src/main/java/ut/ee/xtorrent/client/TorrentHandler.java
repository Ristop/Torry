package ut.ee.xtorrent.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;


// I'm tinking that this class might be responsible for communicating with tracker and client. (one object for a torrent file)
public class TorrentHandler {

    private static final Logger log = LoggerFactory.getLogger(TorrentHandler.class);

    private final Torrent torrent;
    private final String clientPath;
    private final PiecesHandler piecesHandler;

    public TorrentHandler(Torrent torrent, String clientPath, String clien2Path) {
        this.torrent = torrent;
        this.clientPath = clientPath;
        this.piecesHandler = new PiecesHandler(torrent, clientPath);
        log.info("Pieces that this client have: " + this.piecesHandler.getExistingPieces().toString());
        log.info("Pieces that this client do not have: " + this.piecesHandler.getNotExistingPieces().toString());

        //testWritingBytes(clien2Path);
    }

    // todo remove it later
    private void testWritingBytes(String client2) {
        PiecesHandler piecesHandler2 = new PiecesHandler(torrent, client2);
        log.info("Pieces that client2 have: " + piecesHandler2.getExistingPieces().toString());
        log.info("Pieces that client2 do not have: " + piecesHandler2.getNotExistingPieces().toString());
    /*                // code for sending file
        while (piecesHandler2.getNotExistingPieces().size() != 0) {
            int randomPieceID = getRandomPieceID(piecesHandler2.getNotExistingPieces());
            if (randomPieceID != -1) {
                byte[] pieceBytes = piecesHandler.getPieceBytes(randomPieceID);
                piecesHandler2.writePiece(randomPieceID, pieceBytes);
                log.info("Pieces that client2 have: " + piecesHandler2.getExistingPieces().toString());
                log.info("Pieces that client2 do not have: " + piecesHandler2.getNotExistingPieces().toString());
            }
        }*/
    }

    private int getRandomPieceID(List<Integer> list) {
        if (list.size() >= 1) {
            Random random = new Random();
            return list.get(random.nextInt(list.size()));
        }
        return -1;
    }


}
