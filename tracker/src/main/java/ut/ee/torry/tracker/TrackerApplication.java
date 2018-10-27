package ut.ee.torry.tracker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@SpringBootApplication(scanBasePackages = {"ut.ee.torry"})
public class TrackerApplication {

    private static final Logger log = LoggerFactory.getLogger(TrackerApplication.class);

    protected TrackerApplication() {
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(TrackerApplication.class);
        app.setBannerMode(Banner.Mode.LOG);
        app.run(args);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down tracker.");
    }

}
