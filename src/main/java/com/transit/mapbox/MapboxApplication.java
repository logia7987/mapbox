package com.transit.mapbox;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class MapboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(MapboxApplication.class, args);
	}
}
