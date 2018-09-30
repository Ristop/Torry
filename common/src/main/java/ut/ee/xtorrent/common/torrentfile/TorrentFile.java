package ut.ee.xtorrent.common.torrentfile;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TorrentFile {

    private static final Logger log = LoggerFactory.getLogger(TorrentFile.class);

    private final TorrentFileInfo torrentFileInfo;
    private final String announce;

    // optional parameters
    private final List<String> announceList;
    private final Instant creationDate;
    private final String comment;
    private final String createdBy;
    private final String encoding;

    public TorrentFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        TorrentFileParser parser = new TorrentFileParser(Files.readAllBytes(path));

        this.torrentFileInfo = parser.getTorrentFileInfo();
        this.announce = parser.getAnnounce();
        this.announceList = parser.getAnnounceList();
        this.creationDate = parser.getCreationDate();
        this.comment = parser.getComment();
        this.createdBy = parser.getCreatedBy();
        this.encoding = parser.getEncoding();
    }

    public TorrentFileInfo getTorrentFileInfo() {
        return torrentFileInfo;
    }

    public String getAnnounce() {
        return announce;
    }

    public Optional<List<String>> getAnnounceList() {
        if (announceList != null) {
            return Optional.of(announceList);
        } else {
            return Optional.empty();
        }
    }

    public Optional<Instant> getCreationDate() {
        if (creationDate != null) {
            return Optional.of(creationDate);
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getComment() {
        if (comment != null) {
            return Optional.of(comment);
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getCreatedBy() {
        if (createdBy != null) {
            return Optional.of(createdBy);
        } else {
            return Optional.empty();
        }
    }

    public Optional<String> getEncoding() {
        if (encoding != null) {
            return Optional.of(encoding);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public String toString() {
        return "TorrentFile{" +
                "torrentFileInfo=" + torrentFileInfo +
                ", announce='" + announce + '\'' +
                ", announceList=" + announceList +
                ", creationDate=" + creationDate +
                ", comment='" + comment + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", encoding='" + encoding + '\'' +
                '}';
    }

    private static class TorrentFileParser {

        private final TorrentFileInfo torrentFileInfo;
        private final String announce;
        private final List<String> announceList;
        private final Instant creationDate;
        private final String comment;
        private final String createdBy;
        private final String encoding;

        @SuppressWarnings("unchecked")
        public TorrentFileParser(byte[] bytes) {
            Map<String, Object> dict = getDecodedMap(bytes);

            Map<String, Object> info = (Map<String, Object>) dict.get("info");
            this.torrentFileInfo = new TorrentFileInfo(info);

            this.announce = (String) dict.get("announce");

            // optional parameters
            this.announceList = (List<String>) dict.getOrDefault("announce-list", null);
            this.creationDate = dict.containsKey("creation-date") ? Instant.parse((String) dict.get("creation-date")) : null;
            this.comment = dict.containsKey("comment") ? (String) dict.get("comment") : null;
            this.createdBy = dict.containsKey("created-by") ? (String) dict.get("created-by") : null;
            this.encoding = dict.containsKey("encoding") ? (String) dict.get("encoding") : null;
        }

        private Map<String, Object> getDecodedMap(byte[] bytes) {
            Bencode bencode = new Bencode(UTF_8);
            return bencode.decode(bytes, Type.DICTIONARY);
        }

        public TorrentFileInfo getTorrentFileInfo() {
            return torrentFileInfo;
        }

        public String getAnnounce() {
            return announce;
        }

        public List<String> getAnnounceList() {
            return announceList;
        }

        public Instant getCreationDate() {
            return creationDate;
        }

        public String getComment() {
            return comment;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public String getEncoding() {
            return encoding;
        }

    }

}
