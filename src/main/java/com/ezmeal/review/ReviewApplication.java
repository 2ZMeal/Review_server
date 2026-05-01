package com.ezmeal.review;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

// Feign Client 적용
@EnableFeignClients
// 공통 모듈(common)도 스캔 대상으로 추가하여 빈(Bean)들을 함께 읽어옴
@SpringBootApplication(scanBasePackages = {"com.ezmeal.review", "com.ezmeal.common"})
public class ReviewApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReviewApplication.class, args);
	}

}
