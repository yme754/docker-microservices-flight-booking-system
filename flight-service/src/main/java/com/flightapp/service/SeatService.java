package com.flightapp.service;

import java.util.List;

import com.flightapp.entity.Seat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SeatService {
	Flux<Seat> getSeatsByFlightId(String flightId);
    Mono<Void> updateSeats(String flightId, List<Seat> seats);
    Mono<Void> bookSeats(String flightId, List<String> seatNumbers);
	Mono<Void> addSeats(String flightId, List<Seat> seats);
}
