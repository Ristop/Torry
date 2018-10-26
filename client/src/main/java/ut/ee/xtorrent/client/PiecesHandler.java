package ut.ee.xtorrent.client;

import be.christophedetroyer.torrent.Torrent;
import be.christophedetroyer.torrent.TorrentFile;
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
    private final int piecesCount;
    private final Helper helper;

    private List<Integer> existingPieces;
    private List<Integer> notExistingPieces;


    PiecesHandler(Torrent torrent, String clientPath){
        this.torrent = torrent;
        this.clientPath = clientPath;

        this.helper = new Helper(torrent, clientPath);
        this.piecesCount = helper.findPiecesCount();
        List<List<Integer>> existences = helper.getPiecesExistence();
        this.existingPieces = existences.get(0);
        this.notExistingPieces = existences.get(1);
    }

    public List<Integer> getExistingPieces(){
        return this.existingPieces;
    }

    public int getPiecesCount(){
        return this.piecesCount;
    }

    public List<Integer> getNotExistingPieces(){
        return this.notExistingPieces;
    }

    public Piece getPiece(int id) {
        if (torrent.isSingleFileTorrent()) {
            return helper.getSingleFilePiece(id);
        } else {
            return null;                            //todo multiple file torrent case
        }
    }

    public byte[] getPieceBytes(int id) {
        return getPiece(id).getBytes();
    }

    public void writePiece(int id, byte[] bytes) {
        Piece piece = new Piece(id, this.torrent, bytes);
        piece.writeBytes(this.clientPath);
        if (piece.isCorrect()) {
            this.notExistingPieces.remove(new Integer(id));
            this.existingPieces.add(id);
        }
    }




    private class Helper {

        private final Logger log = LoggerFactory.getLogger(PiecesHandler.class);
        private final Torrent torrent;
        private final String clientPath;
        private final int piecesCount;

        Helper(Torrent torrent, String clientPath){
            this.torrent = torrent;
            this.clientPath = clientPath;
            this.piecesCount = findPiecesCount();
        }

        private List<List<Integer>> getPiecesExistence() {
            File torrentDirOrFile = new File(this.clientPath + "/" + this.torrent.getName());
            List<Integer> existing = new ArrayList<>();
            List<Integer> notExisting = new ArrayList<>();
            if (torrentDirOrFile.isDirectory()){
                return findPiecesExistanceFromDirectory();
            }
            else if (torrentDirOrFile.isFile()) {                                       // single file case
                return addPiecesExistanceFromFile(torrentDirOrFile, existing, notExisting, 0);
            }
            else                                                    // this file/folder doesn't exist
                return allPiecesMissing(existing, notExisting);
        }

        private List<List<Integer>> allPiecesMissing(List<Integer> existing, List<Integer> notExisting) {
            for (int i = 0; i<this.piecesCount; i++) {
                notExisting.add(i);
            }
            return listOfLists(existing, notExisting);
        }

        private List<List<Integer>> findPiecesExistanceFromDirectory() {
            return listOfLists(new ArrayList<>(), new ArrayList<>());               //todo
        }

        private List<List<Integer>> addPiecesExistanceFromFile(File file, List<Integer> existing, List<Integer> notExisting, int firstPieceID)  {
            try{
                List<List<Integer>> pieces = getPiecesExistanceFromFile(file, firstPieceID);
                existing.addAll(pieces.get(0));
                notExisting.addAll(pieces.get(1));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return listOfLists(existing, notExisting);
        }

        private List<List<Integer>> getPiecesExistanceFromFile(File file, int firstPieceID) throws IOException{
            List<Integer> existing = new ArrayList<>();
            List<Integer> notExisting = new ArrayList<>();
            int pieceSize = torrent.getPieceLength().intValue();
            byte[] fileContent = Files.readAllBytes(file.toPath());

            for (int pieceID = 0; pieceID < this.piecesCount; pieceID++) {
                byte[] currentPieceBytes = Arrays.copyOfRange(fileContent, pieceSize * pieceID, (pieceSize * (pieceID + 1)));
                Piece piece = new Piece(pieceID + firstPieceID, this.torrent, currentPieceBytes);
                if (piece.isCorrect())                   // verifying if the bytes really correspond to torrent file metadata
                    existing.add(piece.getId());
                else
                    notExisting.add(piece.getId());
            }
            return listOfLists(existing, notExisting);
        }

        private List<List<Integer>> listOfLists(List<Integer> list1, List<Integer> list2){
            List<List<Integer>> res = new ArrayList<>();
            res.add(list1);
            res.add(list2);
            return res;
        }

        private int findPiecesCount() {
            if (this.piecesCount == 0) {
                double pieceSize = this.torrent.getPieceLength();
                if (this.torrent.isSingleFileTorrent()) {
                    return (int) (Math.ceil((this.torrent.getTotalSize().doubleValue() / pieceSize)));
                } else {
                    double totalSize = 0;
                    for (TorrentFile file : this.torrent.getFileList()) {
                        totalSize = totalSize + file.getFileLength().intValue();
                    }
                    return (int) Math.ceil(totalSize / this.torrent.getPieceLength().doubleValue());
                }
            } else {
                return this.piecesCount;
            }
        }

        private Piece getSingleFilePiece(int id) {
            File file = new File(this.clientPath + "/" + this.torrent.getName());
            try{
                byte[] fileContent = Files.readAllBytes(file.toPath());
                byte[] currentPieceBytes = Arrays.copyOfRange(fileContent, this.torrent.getPieceLength().intValue()* id,
                        (this.torrent.getPieceLength().intValue() * (id + 1)));
                Piece piece = new Piece(id, this.torrent, currentPieceBytes);
                if (piece.isCorrect())
                    return piece;
                else {
                    throw new RuntimeException("Piece is either not downloaded or there's a mistake in the code");
                }
            } catch (IOException e){
                e.printStackTrace();
                return null;
            }
        }

    }
}
