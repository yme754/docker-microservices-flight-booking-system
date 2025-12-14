package com.flightapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@SpringBootApplication
@EnableEurekaServer
public class FlightServiceRegisteryApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightServiceRegisteryApplication.class, args);
	}

}