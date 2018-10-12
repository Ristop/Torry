package ut.ee.xtorrent.common.torrentfile;


import bencoding.types.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TorrentFileInfo {

    private final long pieceLength;
    private final List<String> pieces;
    private final boolean isPrivate;

    private final InfoDict infoDict;
    private final boolean singleFile;

    public TorrentFileInfo(BDictionary infoDict) {

        this.pieceLength = ((BInt) infoDict.find(new BByteString("piece length"))).getValue();
        this.isPrivate = parseIsPrivate(infoDict);
        this.pieces = parsePieces(infoDict);

        if (infoDict.find(new BByteString("length")) != null) {
            // single file mode
            this.infoDict = new SingleFileInfoDict(infoDict);
            this.singleFile = true;
        } else {
            // multiple file mode
            this.infoDict = new MultipleFileInfoDict(infoDict);
            this.singleFile = false;
        }
    }

    private boolean parseIsPrivate(BDictionary dict) {
        if (dict.find(new BByteString("private")) != null) {
            long privateValue = ((BInt) dict.find(new BByteString("private"))).getValue();
            if (privateValue == 1)
                return true;
        }
        return false;
    }

    private List<String> parsePieces(BDictionary dict) {
        byte[] piecesBytes = ((BByteString) dict.find(new BByteString("pieces"))).getData();
        List<String> hashes = new ArrayList<>();
        if (piecesBytes.length % 20 == 0) {
            int hashCount = piecesBytes.length / 20;
            for (int currHash = 0; currHash < hashCount; currHash++) {
                byte[] currHashBytes = Arrays.copyOfRange(piecesBytes, currHash * 20, (currHash + 1) * 20);
                String sha1 = bytesToHex(currHashBytes);
                hashes.add(sha1);
            }
        } else {
            throw new RuntimeException("Pieces bytes count is not multiple of 20");
        }
        return hashes;
    }

    // taken from https://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public long getPieceLength() {
        return pieceLength;
    }

    public List<String> getPieces() {
        return pieces;
    }

    public boolean isPrivate() {
        return isPrivate;
    }

    public InfoDict getInfoDict() {
        return infoDict;
    }

    public boolean isSingleFile() {
        return singleFile;
    }

    @Override
    public String toString() {
        return "TorrentFileInfo{" +
                "pieceLength=" + pieceLength +
//                ", pieces='" + pieces + '\'' +
                ", isPrivate=" + isPrivate +
                ", infoDict=" + infoDict +
                ", singleFile=" + singleFile +
                '}';
    }

}
