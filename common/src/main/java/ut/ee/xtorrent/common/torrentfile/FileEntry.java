package ut.ee.xtorrent.common.torrentfile;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FileEntry {

    private final long length;
    private final String md5sum;
    private final String path;

    @SuppressWarnings("unchecked")
    public FileEntry(Map<String, Object> fileEntry) {
        this.length = (long) fileEntry.get("length");
        this.md5sum = fileEntry.containsKey("md5sum") ? (String) fileEntry.get("md5sum") : null;
        this.path = String.join("", (List<String>) fileEntry.get("path"));
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

    public String getPath() {
        return path;
    }

    @Override
    public String toString() {
        return "FileEntry{" +
                "length=" + length +
                ", md5sum='" + md5sum + '\'' +
                ", path='" + path + '\'' +
                '}';
    }

}
