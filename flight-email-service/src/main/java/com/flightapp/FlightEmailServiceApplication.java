package com.flightapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class FlightEmailServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightEmailServiceApplication.class, args);
	}

}