package uk.gov.hmcts.dev.config.extensions;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class RedisTestContainerConfiguration {
    @Bean
    @ServiceConnection
    public RedisContainer redisContainer(){
        return new RedisContainer(DockerImageName.parse("valkey/valkey:9.0.1-alpine"));
    }
}
