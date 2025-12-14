package com.flightapp.service.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flightapp.entity.Flight;
import com.flightapp.entity.Price;
import com.flightapp.entity.Seat;
import com.flightapp.repository.FlightRepository;
import com.flightapp.repository.SeatRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class FlightSImplementationTest {
	@Mock
    private FlightRepository flightRepo;

    @Mock
    private SeatRepository seatRepo;

    @InjectMocks
    private FlightSImplementation service;

    private Flight flight;
    private Seat seat;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        flight = new Flight(UUID.randomUUID().toString(),"HYD", "DEL", LocalDateTime.now().plusDays(1),
        		LocalDateTime.now().plusDays(1).plusHours(2),100,new Price(2000, 3500),"A1","6E101"
        );
        seat = new Seat("S1", "1A", true, flight.getId());
    }

    @Test
    void testGetFlightById() {
        when(flightRepo.findById(flight.getId())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.getFlightById(flight.getId())).expectNext(flight).verifyComplete();
        verify(flightRepo).findById(flight.getId());
    }

    @Test
    void testUpdateFlight() {
        when(flightRepo.save(any())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.updateFlight(flight.getId(), flight)).verifyComplete();
        verify(flightRepo).save(any());
    }

    @Test
    void testGetSeatsByFlightId() {
        when(seatRepo.findByFlightId(flight.getId())).thenReturn(Flux.just(seat));
        StepVerifier.create(service.getSeatsByFlightId(flight.getId())).expectNext(seat).verifyComplete();
        verify(seatRepo).findByFlightId(flight.getId());
    }

    @Test
    void testUpdateSeats() {
        when(seatRepo.findByFlightId(flight.getId())).thenReturn(Flux.just(seat));
        when(seatRepo.delete(seat)).thenReturn(Mono.empty());
        when(seatRepo.save(any())).thenReturn(Mono.just(seat));
        StepVerifier.create(service.updateSeats(flight.getId(), List.of(seat))).verifyComplete();
    }

    @Test
    void testReduceAvailableSeats_Success() {
        when(flightRepo.findById(flight.getId())).thenReturn(Mono.just(flight));
        when(flightRepo.save(any())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.reduceAvailableSeats(flight.getId(), 5)).expectNextCount(1).verifyComplete();
        verify(flightRepo).save(any());
    }

    @Test
    void testReduceAvailableSeats_NotEnoughSeats() {
        flight.setAvailableSeats(1);
        when(flightRepo.findById(flight.getId())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.reduceAvailableSeats(flight.getId(), 5))
        .expectErrorMatches(ex -> ex.getMessage().equals("Not enough seats available")).verify();
    }

    @Test
    void testIncreaseAvailableSeats() {
        when(flightRepo.findById(flight.getId())).thenReturn(Mono.just(flight));
        when(flightRepo.save(any())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.increaseAvailableSeats(flight.getId(), 20)).expectNext(flight).verifyComplete();
    }

    @Test
    void testAddFlight() {
        when(flightRepo.save(any())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.addFlight(flight)).expectNext(flight).verifyComplete();
        verify(flightRepo).save(flight);
    }

    @Test
    void testSearchFlights() {
        when(flightRepo.findAll()).thenReturn(Flux.just(flight));
        StepVerifier.create(service.searchFlights("HYD", "DEL")).expectNext(flight).verifyComplete();
    }
}
