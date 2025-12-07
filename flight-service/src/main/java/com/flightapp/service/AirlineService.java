package com.flightapp.service;

import com.flightapp.entity.Airline;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface AirlineService {
	Flux<Airline> getAllAirlines();
    Mono<Airline> getById(String id);
    Mono<Airline> addAirline(Airline airline);
    Mono<Airline> addFlightToAirline(String airlineId, String flightId);
}
