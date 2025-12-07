package com.flightapp.repository;


import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.flightapp.entity.Flight;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface FlightRepository extends ReactiveMongoRepository<Flight, String>{
	Flux<Flight> findByFromPlaceAndToPlace(String fromPlace, String toPlace);
	Mono<Flight> findByFlightNumber(String flightNumber);
}
