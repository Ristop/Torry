package ut.ee.xtorrent.common.torrentfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MultipleFileInfoDict extends InfoDict {

    private final List<FileEntry> files;

    @SuppressWarnings("unchecked")
    public MultipleFileInfoDict(Map<String, Object> info) {
        super(info);

        List<Map<String, Object>> files = (List<Map<String, Object>>) info.get("files");

        List<FileEntry> fileEntries = new ArrayList<>();
        for (Map<String, Object> fileEntry : files) {
            fileEntries.add(new FileEntry(fileEntry));
        }
        this.files = fileEntries;
    }

    public List<FileEntry> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return "MultipleFileInfoDict{" +
                "files=" + files +
                '}';
    }

}
