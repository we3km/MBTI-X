package com.kh.mbtix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


@EnableScheduling
@SpringBootApplication
public class MbtixApplication {

	public static void main(String[] args) {
		SpringApplication.run(MbtixApplication.class, args);
	}
}
