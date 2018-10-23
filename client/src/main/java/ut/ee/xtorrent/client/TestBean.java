package ut.ee.xtorrent.client;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TestBean {

    private static final Logger log = LoggerFactory.getLogger(TestBean.class);

    public TestBean(@Qualifier(ClientConfiguration.CLIENT_CONFIG) Config clientConf) {
        log.info("port: {} ", clientConf.getInt("port"));
    }
}
