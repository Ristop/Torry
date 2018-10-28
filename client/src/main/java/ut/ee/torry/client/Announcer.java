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
            String trackerURL,
            String infoHash,
            String peerId,
            int port,
            long uploaded,
            long downloaded,
            long left
    ) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(trackerURL);
        uriBuilder.addParameter("info_hash", infoHash);
        uriBuilder.addParameter("peer_id", peerId);
        uriBuilder.addParameter("port", String.valueOf(port));
        uriBuilder.addParameter("uploaded", String.valueOf(uploaded));
        uriBuilder.addParameter("downloaded", String.valueOf(downloaded));
        uriBuilder.addParameter("left", String.valueOf(left));

        return restTemplate.getForObject(uriBuilder.toString(), TrackerResponse.class);
    }

}
