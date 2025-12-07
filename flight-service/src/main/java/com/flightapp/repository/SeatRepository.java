package com.flightapp.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.entity.Seat;

import reactor.core.publisher.Flux;

@Repository
public interface SeatRepository extends ReactiveMongoRepository<Seat, String>{
	Flux<Seat> findByFlightId(String flightId);
}
