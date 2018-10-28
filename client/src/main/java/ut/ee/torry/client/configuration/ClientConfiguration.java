package ut.ee.torry.client.configuration;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Random;

@Configuration
public class ClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(ClientConfiguration.class);

    private static Random r = new Random();

    private static final String MAIN_CONFIG = "mainConfig";
    private static final String CLIENT_CONFIG = "clientConfig";

    public static final String PORT = "port";
    public static final String TORRENT_FILES_DIR = "torrentFilesDir";
    public static final String DOWNLOADED_FILES_DIR = "downloadedFilesDir";
    public static final String PEER_ID = "peerId";

    private static final String CLIENT_ID_PREFIX = "TR";
    private static final String CLIENT_VERSION_NR = "0001";

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

    /**
     * @return Azureus-style peer-id
     * format: ('-', two characters for client id, four ascii digits for version number, '-', 12 random digits)
     * returned String is exactly 20 bytes (characters) long.
     */
    @Bean(PEER_ID)
    public String peerId() {
        return "-" + CLIENT_ID_PREFIX + CLIENT_VERSION_NR + "-" + get12DigitRandomNumber();
    }

    private static String get12DigitRandomNumber() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append((char) ('0' + r.nextInt(10)));
        }
        return sb.toString();
    }

}
