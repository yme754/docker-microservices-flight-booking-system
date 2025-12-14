package com.flightapp.security.config;

import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import com.flightapp.security.jwt.JwtUtils;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthenticationFilter implements ServerSecurityContextRepository {

    private final JwtUtils jwtUtils;
    private final ReactiveAuthenticationManager authenticationManager;

    public JwtAuthenticationFilter(JwtUtils jwtUtils, ReactiveAuthenticationManager authenticationManager) {
        this.jwtUtils = jwtUtils;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtils.validateJwtToken(token)) {
                String username = jwtUtils.getUserNameFromJwtToken(token);
                Authentication auth = new UsernamePasswordAuthenticationToken(username, token);
                return authenticationManager.authenticate(auth)
                        .map(SecurityContextImpl::new);
            }
        }
        return Mono.empty();
    }
}
