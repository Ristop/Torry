package ut.ee.torry.client.configuration;

import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class NetworkConfiguration {

    @Bean
    public RestTemplate getRestTemplate() {
        HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
                new HttpComponentsClientHttpRequestFactory(
                        HttpClientBuilder.create().build());

        clientHttpRequestFactory.setConnectTimeout(2_000);
        clientHttpRequestFactory.setConnectionRequestTimeout(2_000);
        clientHttpRequestFactory.setReadTimeout(10_000);

        return new RestTemplate(clientHttpRequestFactory);
    }

}
