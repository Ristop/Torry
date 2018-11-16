package ut.ee.torry.client;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import ut.ee.torry.common.TrackerResponse;

import java.net.URISyntaxException;
import java.util.Objects;

@Component
public class Announcer {

    private static final Logger log = LoggerFactory.getLogger(Announcer.class);

    private final RestTemplate restTemplate;

    @Autowired
    public Announcer(RestTemplate restTemplate) {
        this.restTemplate = Objects.requireNonNull(restTemplate);
    }

    public TrackerResponse announce(
            AnnounceParams params
    ) {
        URIBuilder uriBuilder;
        try {
            uriBuilder = new URIBuilder(params.getTrackerURL());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
        uriBuilder.addParameter("info_hash", params.getInfoHash());
        uriBuilder.addParameter("peer_id", params.getPeerId());
        uriBuilder.addParameter("port", String.valueOf(params.getPort()));
        uriBuilder.addParameter("uploaded", String.valueOf(params.getUploaded()));
        uriBuilder.addParameter("downloaded", String.valueOf(params.getDownloaded()));
        uriBuilder.addParameter("left", String.valueOf(params.getLeft()));
        String event = params.getEvent();
        if (event != null) {
            uriBuilder.addParameter("event", String.valueOf(event));
        }

        return restTemplate.getForObject(uriBuilder.toString(), TrackerResponse.class);
    }

    public static class AnnounceParams {

        private final String trackerURL;
        private final String infoHash;
        private final String peerId;
        private final int port;
        private final long uploaded;
        private final long downloaded;
        private final long left;
        private String event;

        public AnnounceParams(
                String trackerURL, String infoHash, String peerId, int port, long uploaded, long downloaded, long left
        ) {
            this.trackerURL = trackerURL;
            this.infoHash = infoHash;
            this.peerId = peerId;
            this.port = port;
            this.uploaded = uploaded;
            this.downloaded = downloaded;
            this.left = left;
        }

        public AnnounceParams withEvent(String event) {
            this.event = event;
            return this;
        }

        public String getTrackerURL() {
            return trackerURL;
        }

        public String getInfoHash() {
            return infoHash;
        }

        public String getPeerId() {
            return peerId;
        }

        public int getPort() {
            return port;
        }

        public long getUploaded() {
            return uploaded;
        }

        public long getDownloaded() {
            return downloaded;
        }

        public long getLeft() {
            return left;
        }

        public String getEvent() {
            return event;
        }

    }

}
