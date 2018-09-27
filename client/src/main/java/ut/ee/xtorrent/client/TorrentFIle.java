package ut.ee.xtorrent.client;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class TorrentFIle {

    private static final Logger log = LoggerFactory.getLogger(ClientConfiguration.class);
    private String announce;                               // tracker URL
    private ArrayList<String> announce_list;               // list of all trackers (optional field)
    private long piece_length;                             // number of bytes that each piece is split into
    private ArrayList<HashMap> files;                      // Contains paths and sizes about all the files
    private long length;                                   // shows the size of the file in bytes (single file download)
    private String name;


    TorrentFIle(String location) throws IOException{
        Path path = Paths.get(location);
        byte[] data = Files.readAllBytes(path);
        GetFileData(data);
    }

    @SuppressWarnings("unchecked")
    private void GetFileData(byte[] data){
        Bencode bencode = new Bencode();
        Map<String, Object> dict = bencode.decode(data, Type.DICTIONARY);
        Map<String, Object> infoMap = (Map<String, Object>) dict.get("info");

        this.announce = dict.get("announce").toString();
        this.piece_length = (long) infoMap.get("piece length");
        if (infoMap.containsKey("length")) {                                   // happens if you want to download a single file
            this.length = (long) (infoMap.get("length"));
        } else {                                                               // works if you want to download a directory or multiple files
            this.files = (ArrayList<HashMap>) infoMap.get("files");
        }

        if (infoMap.containsKey("name"))                       // Here start optional fields
            this.name = infoMap.get("name").toString();
        if (dict.containsKey("announce-list"))
            this.announce_list = (ArrayList<String>) dict.get("announce-list");


        // todo: Pieces part is not done yey
        //log.info(infoMap.get("pieces").toString());          Janar will deal with the "pieces" part?
    }

    public String getAnnounce() {
        return announce;
    }

    public ArrayList<String> getAnnounce_list() {
        return announce_list;
    }

    public long getPiece_length() {
        return piece_length;
    }

    public ArrayList<HashMap> getFiles() {
        return files;
    }

    public long getLength() {
        return length;
    }

    public String getName() {
        return name;
    }

    public String toString(){
        return "Torrent file content: \n" +
                "Name : " + getName() + "\n" +
                "Announce : " + getAnnounce() + "\n" +
                "Announce list : " + getAnnounce_list() + "\n" +
                "One piece size :" + getPiece_length() + "\n" +
                "File total size :" + getLength() + "\n" +
                "All files :" + getFiles();
    }
}
