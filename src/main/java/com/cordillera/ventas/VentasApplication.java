package com.cordillera.ventas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@SpringBootApplication(scanBasePackages = "com.cordillera")
@EnableFeignClients
public class VentasApplication {
	public static void main(String[] args) {
		SpringApplication.run(VentasApplication.class, args);
	}
}