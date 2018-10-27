package ut.ee.torry.client;

import be.christophedetroyer.torrent.TorrentParser;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Configuration
public class ClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ClientConfiguration.class);
    private static Config clientConf;

    // Client application logic starts from here
    public ClientConfiguration() {
        log.info("Client Configurations Initialized.");
    }

    public static final String MAIN_CONFIG = "mainConfig";
    public static final String CLIENT_CONFIG = "clientConfig";

    @Bean(name = MAIN_CONFIG)
    public Config config() {
        log.info("Loading configuration.");
        return ConfigFactory.load();
    }

    @Bean(name = CLIENT_CONFIG)
    public Config clientConfig(
            @Qualifier(MAIN_CONFIG) Config config
    ) {
        log.info("Loading client configuration.");
        this.clientConf = config.getConfig("client");
        return config.getConfig("client");
    }

    private void readTorrentFiles() {
        log.info("Torrent files testing started");

        try (Stream<Path> paths = Files.walk(Paths.get("common/src/main/resources/test_torrent_files"))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(f -> {
                        try {
                            System.out.println(TorrentParser.parseTorrent(f.toString()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    })
            ;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
