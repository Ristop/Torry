package ut.ee.torry.client.temp;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PiecesHandler {

    private static final Logger log = LoggerFactory.getLogger(PiecesHandler.class);

    private final Torrent torrent;
    private final String clientPath;
    private final List<Integer> existingPieces;

    PiecesHandler(Torrent torrent, String clientPath) {
        this.torrent = torrent;
        this.clientPath = clientPath;
        this.existingPieces = findExistingPieces();
    }

    private List<Integer> findExistingPieces() {
        List<Integer> existingPieces = new ArrayList<>();
        File torrentDirOrFile = new File(this.clientPath + "/" + this.torrent.getName());
        if (torrentDirOrFile.isDirectory()) {
            return findExistingPiecesFromDirectory();
        } else if (torrentDirOrFile.isFile()) {                                       // single file case
            return findExistingPiecesFromFile(torrentDirOrFile, existingPieces, 0);
        } else                                                                        // this file/folder doesn't exist //todo check it
            return existingPieces;
    }

    private List<Integer> findExistingPiecesFromDirectory() {
        return new ArrayList<>();   //todo
    }

    private List<Integer> findExistingPiecesFromFile(File file, List<Integer> existingPieces, int currentPeaceID) {
        try {
            List<Integer> pieces = getFilePieces(file, currentPeaceID);
            existingPieces.addAll(pieces);
        } catch (IOException e) {
            e.printStackTrace();
            log.error(e.toString());
        }
        return existingPieces;
    }

    private List<Integer> getFilePieces(File file, int currentPeaceID) throws IOException {
        List<Integer> pieces = new ArrayList<>();
        int pieceSize = torrent.getPieceLength().intValue();
        long fileSize = torrent.getTotalSize();
        long piecesCount = fileSize / pieceSize;
        byte[] fileContent = Files.readAllBytes(file.toPath());

        for (int pieceID = 0; pieceID < piecesCount; pieceID++) {
            byte[] currentPieceBytes = Arrays.copyOfRange(fileContent, pieceSize * pieceID, (pieceSize * (pieceID + 1)));
            Piece piece = new Piece(pieceID + currentPeaceID, this.torrent, currentPieceBytes);
            if (piece.isCorrect())                   // verifying if the bytes really correspond to torrent file metadata
                pieces.add(piece.getId());
        }
        return pieces;
    }

    public List<Integer> getexistingPieces() {
        return this.existingPieces;
    }

    //todo add method where we can access byte[] after knowing the id of the piece
}
