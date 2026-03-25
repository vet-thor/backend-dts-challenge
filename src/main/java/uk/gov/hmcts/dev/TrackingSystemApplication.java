package uk.gov.hmcts.dev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import uk.gov.hmcts.dev.config.properties.ApiProperties;
import uk.gov.hmcts.dev.config.properties.JwtProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, ApiProperties.class})
public class TrackingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackingSystemApplication.class, args);
	}

}
