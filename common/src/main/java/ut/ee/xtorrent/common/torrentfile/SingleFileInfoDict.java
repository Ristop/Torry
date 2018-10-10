package ut.ee.xtorrent.common.torrentfile;

import java.util.Map;
import java.util.Optional;

public class SingleFileInfoDict extends InfoDict {

    private final long length;
    private final String md5sum;
    private final String name;

    public SingleFileInfoDict(Map<String, Object> info) {
        super(info);
        this.length = (long) info.get("length");
        this.name = (String) info.get("name");
        this.md5sum = info.containsKey("md5sum") ? (String) info.get("md5sum") : null;
    }

    public long getLength() {
        return length;
    }

    public String getName() {
        return name;
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
                "name=" + name +
                ", length=" + length +
                ", md5sum='" + md5sum + '\'' +
                '}';
    }

}
