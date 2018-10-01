package ut.ee.xtorrent.common.torrentfile;

import java.util.Map;

public class TorrentFileInfo {

    private final long pieceLength;
    private final String pieces;
    private final boolean isPrivate;

    private final InfoDict infoDict;
    private final boolean singleFile;

    public TorrentFileInfo(Map<String, Object> info) {
        this.pieceLength = (long) info.get("piece length");
        this.isPrivate = info.containsKey("private") && (boolean) info.get("private");

        // need to parse this correctly (omitting it from toString for now)
        this.pieces = (String) info.get("pieces");

        if (info.containsKey("length")) {
            // single file mode
            this.infoDict = new SingleFileInfoDict(info);
            this.singleFile = true;
        } else {
            // multiple file mode
            this.infoDict = new MultipleFileInfoDict(info);
            this.singleFile = false;
        }
    }

    public long getPieceLength() {
        return pieceLength;
    }

    public String getPieces() {
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
