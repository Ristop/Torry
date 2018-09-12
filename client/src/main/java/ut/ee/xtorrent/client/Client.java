package ut.ee.xtorrent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Client {

    private static final Logger log = LoggerFactory.getLogger(Client.class);

    // Client application logic starts from here
    public Client() {
        log.info("Client Initialized.");
    }

}
