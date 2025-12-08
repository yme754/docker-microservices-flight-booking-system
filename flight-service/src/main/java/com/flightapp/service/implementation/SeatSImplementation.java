package com.flightapp.service.implementation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
    public Mono<Void> addSeats(String flightId, List<Seat> seats) {
        return Flux.fromIterable(seats)
            .flatMap(newSeat -> {
                newSeat.setFlightId(flightId);            
                return seatRepo.findByFlightIdAndSeatNumber(flightId, newSeat.getSeatNumber())
                    .flatMap(existing -> Mono.empty()).switchIfEmpty(seatRepo.save(newSeat)); 
            })
            .then();
    }

    @Override
    public Mono<Void> updateSeats(String flightId, List<Seat> seats) {
        return seatRepo.findByFlightId(flightId)
                .flatMap(seatRepo::delete)
                .thenMany(Flux.fromIterable(seats))
                .flatMap(seatRepo::save)
                .then();
    }

    @Override
    public Mono<Void> bookSeats(String flightId, List<String> seatNumbers) {
        return seatRepo.findByFlightIdAndSeatNumberIn(flightId, seatNumbers)
                .collectList()
                .flatMap(seats -> {
                    if (seats.size() != seatNumbers.size()) return Mono.<Void>error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more selected seats do not exist on this flight"));
                    for (Seat seat : seats) {
                        if (!seat.isAvailable()) return Mono.<Void>error(new ResponseStatusException(HttpStatus.CONFLICT, "Seat " + seat.getSeatNumber() + " is already booked"));
                    }
                    seats.forEach(s -> s.setAvailable(false));
                    return seatRepo.saveAll(seats).then();
                });
    }
}
