package com.ezmeal.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication(scanBasePackages = {"com.ezmeal.review", "com.ezmeal.common"})
@EntityScan(basePackages = {"com.ezmeal.review", "com.ezmeal.common"})
@EnableJpaRepositories(basePackages = {"com.ezmeal.review", "com.ezmeal.common"})
public class ReviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewApplication.class, args);
	}

}
