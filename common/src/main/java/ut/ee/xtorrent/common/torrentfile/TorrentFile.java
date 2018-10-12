package ut.ee.xtorrent.common.torrentfile;

import bencoding.Reader;
import bencoding.types.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;


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

    public TorrentFile(Path path) throws IOException {
        TorrentFileParser parser = new TorrentFileParser(path.toString());

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

        public TorrentFileParser(String path) throws IOException{
            Reader r = new Reader(new File(path));
            List<IBencodable> fileContent = r.read();

            // A valid torrentfile should only return a single dictionary.
            if (fileContent.size() != 1)
                throw new RuntimeException("Torrent file is not valid");

            BDictionary torrentDictionary = (BDictionary) fileContent.get(0);
            BDictionary infoDictionary = (BDictionary) torrentDictionary.find(new BByteString("info"));

            this.torrentFileInfo = new TorrentFileInfo(infoDictionary);
            this.announce = torrentDictionary.find(new BByteString("announce")).toString();

            // optional parameters
            this.announceList = parseAnnounceList(torrentDictionary);
            this.creationDate = parseCreationDate(torrentDictionary);
            this.comment = parseComment(torrentDictionary);
            this.createdBy = parseCreatedBy(torrentDictionary);
            this.encoding = parseEncoding(torrentDictionary);
        }

        // method taken completly from https://github.com/m1dnight/torrent-parser
        // some other methods also got inspired from that link.
        private List<String> parseAnnounceList(BDictionary dict) {
            if (dict.find(new BByteString("announce-list")) != null)
            {
                List<String> announceUrls = new LinkedList<String>();

                BList announceList = (BList) dict.find(new BByteString("announce-list"));
                Iterator<IBencodable> subLists = announceList.getIterator();
                while (subLists.hasNext())
                {
                    BList subList = (BList) subLists.next();
                    Iterator<IBencodable> elements = subList.getIterator();
                    while (elements.hasNext())
                    {
                        // Assume that each element is a BByteString
                        BByteString tracker = (BByteString) elements.next();
                        announceUrls.add(tracker.toString());
                    }
                }
                return announceUrls;
            } else
            {
                return null;
            }
        }

        private Instant parseCreationDate(BDictionary dict) {
            if (dict.find(new BByteString("creation date")) != null)
                return Instant.parse(dict.find(new BByteString("creation date")).toString());
            return null;
        }

        private String parseComment(BDictionary dict) {
            if (dict.find(new BByteString("comment")) != null)
                return dict.find(new BByteString("comment")).toString();
            return null;
        }

        private String parseCreatedBy(BDictionary dict) {
            if (dict.find(new BByteString("created-by")) != null)
                return dict.find(new BByteString("created-by")).toString();
            return null;
        }

        private String parseEncoding(BDictionary dict) {
            if (dict.find(new BByteString("encoding")) != null)
                return dict.find(new BByteString("encoding")).toString();
            return null;
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
