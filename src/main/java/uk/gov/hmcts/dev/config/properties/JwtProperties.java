package uk.gov.hmcts.dev.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@ConfigurationProperties("auth.jwt")
public record JwtProperties(String secret, Duration timeout) {
    public Date calculateExpiration(){
        return Date.from(Instant.now().plus(timeout()));
    }
}