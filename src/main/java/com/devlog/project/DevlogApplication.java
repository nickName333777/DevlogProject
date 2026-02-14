package com.devlog.project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;



@EnableScheduling  // 스케쥴러 불러오기 위한 임포트
@SpringBootApplication
public class DevlogApplication {

	public static void main(String[] args) {
		SpringApplication.run(DevlogApplication.class, args);
	}

}
