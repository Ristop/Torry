package ut.ee.torry.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@SpringBootApplication(scanBasePackages = {"ut.ee.torry"})
public class ClientApplication {

    private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);

    protected ClientApplication() {
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setBannerMode(Banner.Mode.LOG);
        app.run(args);
    }

    @PostConstruct
    public void startClient() {
        log.info("Starting Client");
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down client.");
    }

}
