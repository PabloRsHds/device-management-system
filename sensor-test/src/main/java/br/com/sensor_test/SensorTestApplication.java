package br.com.sensor_test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SensorTestApplication {

	public static void main(String[] args) {
		SpringApplication.run(SensorTestApplication.class, args);
	}

}
