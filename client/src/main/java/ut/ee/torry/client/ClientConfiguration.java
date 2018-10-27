package ut.ee.torry.client;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ClientConfiguration.class);

    private static final String MAIN_CONFIG = "mainConfig";
    private static final String CLIENT_CONFIG = "clientConfig";

    private static final String PORT = "port";
    private static final String TORRENT_FILES_DIR = "torrentFilesDir";
    private static final String DOWNLOADED_FILES_DIR = "downloadedFilesDir";

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
        return config.getConfig("client");
    }

    // Actual config value beans start here

    @Bean(PORT)
    public int port(
            @Qualifier(CLIENT_CONFIG) Config config
    ) {
        return config.getInt(PORT);
    }

    @Bean(TORRENT_FILES_DIR)
    public String torrentFilesDir(
            @Qualifier(CLIENT_CONFIG) Config config
    ) {
        return config.getString(TORRENT_FILES_DIR);
    }

    @Bean(DOWNLOADED_FILES_DIR)
    public String downloadedFilesDir(
            @Qualifier(CLIENT_CONFIG) Config config
    ) {
        return config.getString(DOWNLOADED_FILES_DIR);
    }

}
