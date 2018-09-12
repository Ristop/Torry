package ut.ee.xtorrent.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Tracker {

    private static final Logger log = LoggerFactory.getLogger(Tracker.class);

    // Tracker application logic starts from here
    public Tracker() {
        log.info("Tracker initialized.");
    }

}
