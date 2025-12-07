package com.flightapp.service.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.flightapp.entity.Airline;
import com.flightapp.repository.AirlineRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class AirlineSImplementationTest {
	@Mock
    private AirlineRepository airlineRepo;
	
    @InjectMocks
    private AirlineSImplementation service;
    private Airline airline;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        airline = new Airline(UUID.randomUUID().toString(),"Indigo","http://logo.com/a.png",
        		new ArrayList<>(List.of("F1"))
        );
    }

    @Test
    void testGetAllAirlines() {
        when(airlineRepo.findAll()).thenReturn(Flux.just(airline));
        StepVerifier.create(service.getAllAirlines()).expectNext(airline).verifyComplete();
        verify(airlineRepo).findAll();
    }

    @Test
    void testGetById() {
        when(airlineRepo.findById(airline.getId())).thenReturn(Mono.just(airline));
        StepVerifier.create(service.getById(airline.getId())).expectNext(airline).verifyComplete();
        verify(airlineRepo).findById(airline.getId());
    }

    @Test
    void testAddAirline() {
        when(airlineRepo.findByName(anyString())).thenReturn(Mono.empty());
        when(airlineRepo.save(any())).thenReturn(Mono.just(airline));
        StepVerifier.create(service.addAirline(airline)).expectNext(airline).verifyComplete();
        verify(airlineRepo).save(any());
    }

    @Test
    void testAddFlightToAirline() {
        when(airlineRepo.findById(airline.getId())).thenReturn(Mono.just(airline));
        when(airlineRepo.save(any())).thenReturn(Mono.just(airline));
        StepVerifier.create(service.addFlightToAirline(airline.getId(), "F2")).expectNext(airline).verifyComplete();
        verify(airlineRepo).findById(airline.getId());
        verify(airlineRepo).save(any());
    }
    
    @Test
    void testAddAirline_Success() {
        when(airlineRepo.findByName(anyString())).thenReturn(Mono.empty());
        when(airlineRepo.save(any())).thenReturn(Mono.just(airline));        
        StepVerifier.create(service.addAirline(airline)).expectNext(airline).verifyComplete();
    }

    @Test
    void testAddAirline_Duplicate() {
        when(airlineRepo.findByName(airline.getName())).thenReturn(Mono.just(airline));        
        when(airlineRepo.save(any())).thenReturn(Mono.empty());
        StepVerifier.create(service.addAirline(airline))
                .expectErrorMatches(ex -> ex.getMessage().contains("already exists")).verify();
    }
}
