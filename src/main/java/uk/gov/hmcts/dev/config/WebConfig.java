package uk.gov.hmcts.dev.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Value("${api.version}")
    private String apiVersion;

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(apiVersion,
                HandlerTypePredicate.forAnnotation(RestController.class)
                        // This line prevents the prefix from breaking Swagger/OpenAPI
                        .and(type -> !type.getPackageName().startsWith("org.springdoc"))
        );
    }
}