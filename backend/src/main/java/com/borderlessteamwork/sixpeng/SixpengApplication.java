package com.borderlessteamwork.sixpeng;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SixpengApplication {

	public static void main(String[] args) {
		SpringApplication.run(SixpengApplication.class, args);
	}

}
