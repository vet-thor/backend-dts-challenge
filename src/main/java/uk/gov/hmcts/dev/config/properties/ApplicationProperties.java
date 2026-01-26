package uk.gov.hmcts.dev.config.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
@ConfigurationProperties
public class ApplicationProperties {
    @Value("${jwt.security.key}")
    private String securityKey;
    @Value("${jwt.security.timeout}")
    private Long jwtTimeout;
}