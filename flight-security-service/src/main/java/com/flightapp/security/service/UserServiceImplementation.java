package com.flightapp.security.service;

import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.flightapp.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImplementation implements ReactiveUserDetailsService{
	private final UserRepository userRepo;
	@Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepo.findByUsername(username)
                .map(UserImplementation::build).cast(UserDetails.class)
                .switchIfEmpty(Mono.error(new UsernameNotFoundException("User Not Found with username: " + username)));
    }
}
