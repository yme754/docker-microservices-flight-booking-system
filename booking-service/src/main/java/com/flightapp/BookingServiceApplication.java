package com.flightapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@EnableFeignClients
@SpringBootApplication
public class BookingServiceApplication {
	@Bean
	@LoadBalanced
	public WebClient.Builder webClientBuilder() {
	    return WebClient.builder();
	}
	public static void main(String[] args) {
		SpringApplication.run(BookingServiceApplication.class, args);
	}

}
