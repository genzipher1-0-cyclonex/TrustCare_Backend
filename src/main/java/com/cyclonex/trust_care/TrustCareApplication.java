package com.cyclonex.trust_care;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TrustCareApplication {

	public static void main(String[] args) {
		SpringApplication.run(TrustCareApplication.class, args);
	}

}
