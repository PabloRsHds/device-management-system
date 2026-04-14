package br.com.device_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@EnableCaching
@SpringBootApplication
public class DeviceManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeviceManagementApplication.class, args);
	}

}
