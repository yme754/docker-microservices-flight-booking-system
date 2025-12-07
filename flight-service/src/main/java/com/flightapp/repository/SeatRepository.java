package com.flightapp.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.entity.Seat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface SeatRepository extends ReactiveMongoRepository<Seat, String>{
	Flux<Seat> findByFlightId(String flightId);
	Flux<Seat> findByFlightIdAndSeatNumberIn(String flightId, List<String> seatNumbers);
	Mono<Seat> findByFlightIdAndSeatNumber(String flightId, String seatNumber);
}
