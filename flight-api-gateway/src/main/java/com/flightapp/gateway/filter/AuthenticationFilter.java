package com.flightapp.gateway.filter;

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.flightapp.gateway.util.JwtUtils;

import reactor.core.publisher.Mono;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config>{
	private final RouteValidator validator;
    private final JwtUtils jwtUtils;

    public AuthenticationFilter(RouteValidator validator, JwtUtils jwtUtils) {
        super(Config.class);
        this.validator = validator;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            if (validator.isSecured.test(exchange.getRequest())) {                
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) 
                    return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
                String authHeader = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                if (authHeader != null && authHeader.startsWith("Bearer ")) authHeader = authHeader.substring(7);
                try {
                	jwtUtils.validateToken(authHeader);
                    List<String> roles = jwtUtils.getRolesFromToken(authHeader);
                    String rolesString = String.join(",", roles);
                    exchange.getRequest().mutate().header("X-Auth-Roles", rolesString).build();
                } catch (Exception e) {
                    System.err.println("Invalid Token: " + e.getMessage());
                    return onError(exchange, "Unauthorized access to application", HttpStatus.UNAUTHORIZED);
                }
            }
            return chain.filter(exchange);
        });
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        return response.setComplete();
    }

    public static class Config {
    }
}
