package com.flightapp.service.implementation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.flightapp.entity.Seat;
import com.flightapp.repository.SeatRepository;
import com.flightapp.service.SeatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SeatSImplementation implements SeatService{
	private final SeatRepository seatRepo;

    @Override
    public Flux<Seat> getSeatsByFlightId(String flightId) {
        return seatRepo.findByFlightId(flightId);
    }

    @Override
    public Mono<Void> updateSeats(String flightId, List<Seat> seats) {
        return seatRepo.findByFlightId(flightId)
                .flatMap(seatRepo::delete)
                .thenMany(Flux.fromIterable(seats))
                .flatMap(seatRepo::save)
                .then();
    }
}
