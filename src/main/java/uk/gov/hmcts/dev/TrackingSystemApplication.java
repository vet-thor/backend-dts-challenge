package uk.gov.hmcts.dev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class TrackingSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrackingSystemApplication.class, args);
	}

}
