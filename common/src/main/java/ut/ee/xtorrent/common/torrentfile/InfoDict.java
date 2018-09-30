package ut.ee.xtorrent.common.torrentfile;

import java.util.Map;

abstract class InfoDict {

    private final String name;

    public InfoDict(Map<String, Object> info) {
        this.name = (String) info.get("name");
    }

    public String getName() {
        return name;
    }

}
