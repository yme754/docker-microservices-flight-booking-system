package com.flightapp.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
	@Bean
	@LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder().filter(addAuthHeaderFilter());
    }

    private ExchangeFilterFunction addAuthHeaderFilter() {
        return (request, next) -> ReactiveSecurityContextHolder.getContext()
            .map(ctx -> ctx.getAuthentication())
            .flatMap(auth -> {
                String roles = auth.getAuthorities().stream().map(a -> a.getAuthority()).reduce((a, b) -> a + "," + b).orElse("");
                ClientRequest newRequest = ClientRequest.from(request).header("X-Auth-Roles", roles).build();
                return next.exchange(newRequest);
            })
            .switchIfEmpty(next.exchange(request));
    }
}
