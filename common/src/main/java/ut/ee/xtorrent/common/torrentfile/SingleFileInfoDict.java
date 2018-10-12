package ut.ee.xtorrent.common.torrentfile;

import bencoding.types.BByteString;
import bencoding.types.BDictionary;
import bencoding.types.BInt;

import java.util.Map;
import java.util.Optional;

public class SingleFileInfoDict extends InfoDict {

    private final long length;
    private final String md5sum;

    public SingleFileInfoDict(BDictionary infoDict) {
        super(infoDict);
        this.length = ((BInt) infoDict.find(new BByteString("length"))).getValue();
        this.md5sum = parseMd5sum(infoDict);
    }

    private String parseMd5sum(BDictionary dict) {
        if (dict.find(new BByteString("md5sum")) != null)
            return dict.find(new BByteString("md5sum")).toString();
        return null;
    }

    public long getLength() {
        return length;
    }


    public Optional<String> getMd5sum() {
        if (md5sum != null) {
            return Optional.of(md5sum);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "SingleFileInfoDict{" +
                "name=" + this.getName() +
                ", length=" + length +
                ", md5sum='" + md5sum + '\'' +
                '}';
    }

}
