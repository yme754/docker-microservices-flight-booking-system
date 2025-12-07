package com.flightapp.service;

import java.util.List;

import com.flightapp.entity.Flight;
import com.flightapp.entity.Seat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FlightService {
	Mono<Flight> getFlightById(String id);
    Mono<Void> updateFlight(String id, Flight flight);
    Flux<Seat> getSeatsByFlightId(String flightId);
    Mono<Void> updateSeats(String flightId, List<Seat> seats);
    Mono<Flight> reduceAvailableSeats(String flightId, int seatCount);
    Mono<Flight> increaseAvailableSeats(String flightId, int seatCount);
	Mono<Flight> addFlight(Flight flight);
	Flux<Flight> searchFlights(String from, String to);
	Flux<Flight> getAllFlights();
}
