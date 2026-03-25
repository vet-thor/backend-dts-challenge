package uk.gov.hmcts.dev.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("api")
public record ApiProperties(String version) {
    public String getAuthEndpoint(){
        return version + "/auth";
    }

    public String getTaskEndpoint(){
        return version + "/tasks";
    }
}
