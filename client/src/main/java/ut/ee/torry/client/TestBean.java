package ut.ee.torry.client;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TestBean {

    private static final Logger log = LoggerFactory.getLogger(TestBean.class);

    public TestBean(@Qualifier(ClientConfiguration.CLIENT_CONFIG) Config clientConf) {
        log.info("port1: {} ", clientConf.getInt("port1"));
        log.info("port2: {} ", clientConf.getInt("port2"));
        log.info("path: {} ", clientConf.getString("path"));
        String path = clientConf.getString("path");
        new Initializer(path);

    }


    private class Initializer{
        Initializer(String path){
            new InitializeClient(path);
        }
    }
}
