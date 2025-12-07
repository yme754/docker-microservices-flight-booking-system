package com.flightapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {"spring.cloud.config.enabled=false"})
class FlightApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

}