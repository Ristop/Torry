package ut.ee.xtorrent.common.torrentfile;

import bencoding.types.BByteString;
import bencoding.types.BDictionary;


abstract class InfoDict {

    private final String name;

    public InfoDict(BDictionary infoDict) {
        this.name = infoDict.find(new BByteString("name")).toString();
    }

    public String getName() {
        return name;
    }
}
