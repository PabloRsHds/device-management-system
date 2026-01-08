package br.com.device_login;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class DeviceLoginApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeviceLoginApplication.class, args);
	}

}
