package com.dv.agro_web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AgroWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(AgroWebApplication.class, args);
	}

}