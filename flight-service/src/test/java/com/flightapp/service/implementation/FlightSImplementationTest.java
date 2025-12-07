package com.flightapp.service.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

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

    @Mock
    private ReactiveMongoTemplate mongoTemplate;

    @InjectMocks
    private FlightSImplementation service;

    private Flight flight;
    private Seat seat;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        flight = new Flight(UUID.randomUUID().toString(), "HYD", "DEL", LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusDays(1).plusHours(2), 100, new Price(2000, 3500), "A1", "6E101");
        seat = new Seat("S1", "1A", true, flight.getId());
    }

    @Test
    void testGetFlightById() {
        when(flightRepo.findById(flight.getId())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.getFlightById(flight.getId())).expectNext(flight).verifyComplete();
    }

    @Test
    void testUpdateFlight() {
        when(flightRepo.save(any())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.updateFlight(flight.getId(), flight)).verifyComplete();
    }

    @Test
    void testGetSeatsByFlightId() {
        when(seatRepo.findByFlightId(flight.getId())).thenReturn(Flux.just(seat));
        StepVerifier.create(service.getSeatsByFlightId(flight.getId())).expectNext(seat).verifyComplete();
    }

    @Test
    void testUpdateSeats() {
        when(seatRepo.findByFlightId(flight.getId())).thenReturn(Flux.just(seat));
        when(seatRepo.delete(seat)).thenReturn(Mono.empty());
        when(seatRepo.save(any())).thenReturn(Mono.just(seat));
        StepVerifier.create(service.updateSeats(flight.getId(), java.util.List.of(seat))).verifyComplete();
    }

    @Test
    void testReduceAvailableSeats_Success() {
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.reduceAvailableSeats(flight.getId(), 5)).expectNext(flight).verifyComplete();
    }

    @Test
    void testReduceAvailableSeats_NotEnoughSeats() {
        when(mongoTemplate.findAndModify(any(Query.class), any(Update.class), any())).thenReturn(Mono.empty());
        StepVerifier.create(service.reduceAvailableSeats(flight.getId(), 5))
                .expectErrorMatches(ex -> ex.getMessage().contains("Not enough seats"))
                .verify();
    }

    @Test
    void testIncreaseAvailableSeats() {
        when(flightRepo.findById(flight.getId())).thenReturn(Mono.just(flight));
        when(flightRepo.save(any())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.increaseAvailableSeats(flight.getId(), 20)).expectNext(flight).verifyComplete();
    }

    @Test
    void testAddFlight_Success() {
        when(flightRepo.findByFlightNumber(anyString())).thenReturn(Mono.empty());
        when(flightRepo.save(any())).thenReturn(Mono.just(flight));
        StepVerifier.create(service.addFlight(flight)).expectNext(flight).verifyComplete();
    }

    @Test
    void testAddFlight_Duplicate() {
        when(flightRepo.findByFlightNumber(flight.getFlightNumber())).thenReturn(Mono.just(flight));
        when(flightRepo.save(any())).thenReturn(Mono.empty());
        StepVerifier.create(service.addFlight(flight))
                .expectErrorMatches(ex -> ex.getMessage().contains("already exists")).verify();
    }

    @Test
    void testSearchFlights() {
        when(flightRepo.findByFromPlaceAndToPlace("HYD", "DEL")).thenReturn(Flux.just(flight));
        StepVerifier.create(service.searchFlights("HYD", "DEL")).expectNext(flight).verifyComplete();
    }
    
    @Test
    void testGetAllFlights() {
    	when(flightRepo.findAll()).thenReturn(Flux.just(flight));
    	StepVerifier.create(service.getAllFlights()).expectNext(flight).verifyComplete();
    }
}
