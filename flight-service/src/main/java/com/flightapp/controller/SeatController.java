package com.flightapp.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.entity.Seat;
import com.flightapp.service.SeatService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/flight/seats")
@RequiredArgsConstructor
public class SeatController {
	private final SeatService seatService;

	@GetMapping("/{flightId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')") 
    public Flux<Seat> getSeatsByFlightId(@PathVariable String flightId) {
        return seatService.getSeatsByFlightId(flightId);
    }
	
	@PostMapping("/{flightId}/add")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')") 
    public Mono<Void> addNewSeats(@PathVariable String flightId, @RequestBody List<Seat> seats) {
        return seatService.addSeats(flightId, seats);
    }

    @PutMapping("/{flightId}/update")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')") 
    public Mono<Void> updateSeats( @PathVariable String flightId, @RequestBody List<Seat> seats) {
        return seatService.updateSeats(flightId, seats);
    }
    
    @PostMapping("/{flightId}/book")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')") 
    public Mono<Void> bookSeats(@PathVariable String flightId, @RequestBody List<String> seatNumbers) {
        return seatService.bookSeats(flightId, seatNumbers);
    }
}
