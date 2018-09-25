package ut.ee.xtorrent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@SpringBootApplication(scanBasePackages = {"ut.ee.xtorrent"})
public class ClientApplication {

    private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);

    protected ClientApplication() {
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setBannerMode(Banner.Mode.LOG);
        app.run(args);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down client.");
    }

}
