package ut.ee.xtorrent.common.torrentfile;

import bencoding.types.*;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MultipleFileInfoDict extends InfoDict {

    private final List<FileEntry> files;

    public MultipleFileInfoDict(BDictionary infoDict) {
        super(infoDict);
        this.files = parseFiles(infoDict);
    }

    private List<FileEntry> parseFiles(BDictionary infoDict){
        BList filesBList = (BList) infoDict.find(new BByteString("files"));

        Iterator<IBencodable> filesIterator = filesBList.getIterator();
        List<FileEntry> fileEntries = new ArrayList<>();
        while (filesIterator.hasNext()) {
            BDictionary fileDict = (BDictionary) filesIterator.next();
            fileEntries.add(new FileEntry(fileDict));
        }
        return fileEntries;
    }

    public List<FileEntry> getFiles() {
        return files;
    }

    @Override
    public String toString() {
        return "MultipleFileInfoDict{" +
                "name=" + this.getName() +
                ", files=" + files +
                '}';
    }

}
