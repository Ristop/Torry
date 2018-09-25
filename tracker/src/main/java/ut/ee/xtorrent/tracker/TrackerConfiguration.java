package ut.ee.xtorrent.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrackerConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TrackerConfiguration.class);

    // Tracker application logic starts from here
    public TrackerConfiguration() {
        log.info("Tracker initialized.");
    }

}
