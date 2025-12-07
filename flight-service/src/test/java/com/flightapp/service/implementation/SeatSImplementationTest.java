package com.flightapp.service.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flightapp.entity.Seat;
import com.flightapp.repository.SeatRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class SeatSImplementationTest {
	@Mock
    private SeatRepository seatRepo;

    @InjectMocks
    private SeatSImplementation service;

    private Seat seat;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        seat = new Seat("S1", "1A", true, "F101");
    }

    @Test
    void testGetSeatsByFlightId() {
        when(seatRepo.findByFlightId("F101")).thenReturn(Flux.just(seat));
        StepVerifier.create(service.getSeatsByFlightId("F101")).expectNext(seat).verifyComplete();
    }

    @Test
    void testUpdateSeats() {
        when(seatRepo.findByFlightId("F101")).thenReturn(Flux.just(seat));
        when(seatRepo.delete(seat)).thenReturn(Mono.empty());
        when(seatRepo.save(seat)).thenReturn(Mono.just(seat));
        StepVerifier.create(service.updateSeats("F101", List.of(seat))).verifyComplete();
    }

    @Test
    void testBookSeats_Success() {
        when(seatRepo.findByFlightIdAndSeatNumberIn(anyString(), any())).thenReturn(Flux.just(seat));
        when(seatRepo.saveAll(any(Iterable.class))).thenReturn(Flux.just(seat));
        StepVerifier.create(service.bookSeats("F101", List.of("1A")))
                .verifyComplete();
    }

    @Test
    void testBookSeats_MismatchCount() {
        when(seatRepo.findByFlightIdAndSeatNumberIn(anyString(), any())).thenReturn(Flux.just(seat));
        StepVerifier.create(service.bookSeats("F101", List.of("1A", "1B"))) // Requesting 2
                .expectErrorMatches(ex -> ex.getMessage().contains("do not exist"))
                .verify();
    }

    @Test
    void testBookSeats_AlreadyBooked() {
        seat.setAvailable(false);
        when(seatRepo.findByFlightIdAndSeatNumberIn(anyString(), any())).thenReturn(Flux.just(seat));
        StepVerifier.create(service.bookSeats("F101", List.of("1A")))
                .expectErrorMatches(ex -> ex.getMessage().contains("already booked")).verify();
    }
    
    @Test
    void testAddSeats() {
        when(seatRepo.findByFlightIdAndSeatNumber(anyString(), anyString())).thenReturn(Mono.empty());
        when(seatRepo.save(any())).thenReturn(Mono.just(seat));
        StepVerifier.create(service.addSeats("F101", List.of(seat))).verifyComplete();
    }
}
