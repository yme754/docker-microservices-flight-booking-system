package com.flightapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Resilience4jTestConfig {
	@Bean
    public io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry circuitBreakerRegistry() {
        return io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry.ofDefaults();
    }

    @Bean
    public io.github.resilience4j.timelimiter.TimeLimiterRegistry timeLimiterRegistry() {
        return io.github.resilience4j.timelimiter.TimeLimiterRegistry.ofDefaults();
    }
}
