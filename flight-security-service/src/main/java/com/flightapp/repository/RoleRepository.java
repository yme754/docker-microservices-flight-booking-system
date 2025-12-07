package com.flightapp.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.flightapp.entity.ERole;
import com.flightapp.entity.Role;

import reactor.core.publisher.Mono;

public interface RoleRepository extends ReactiveMongoRepository<Role, String>{
	Mono<Role> findByName(ERole name);
}
