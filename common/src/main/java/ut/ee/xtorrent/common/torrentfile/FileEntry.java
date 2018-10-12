package ut.ee.xtorrent.common.torrentfile;

import bencoding.types.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class FileEntry {

    private final long length;
    private final String md5sum;
    private final String path;

    public FileEntry(BDictionary fileEntry) {
        this.length = ((BInt) fileEntry.find(new BByteString("length"))).getValue();
        this.md5sum = parseMd5sum(fileEntry);
        this.path = parsePath(fileEntry);
    }

    private String parseMd5sum(BDictionary dict) {
        if (dict.find(new BByteString("md5sum")) != null)
            return dict.find(new BByteString("md5sum")).toString();
        return null;
    }

    private String parsePath(BDictionary dict) {
        BList filePaths = (BList) dict.find(new BByteString("path"));
        List<String> pathContent = new ArrayList<>();
        Iterator<IBencodable> pathIterator = filePaths.getIterator();
        while (pathIterator.hasNext())
            pathContent.add(pathIterator.next().toString());
        return String.join("/", pathContent);
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
