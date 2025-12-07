package com.flightapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightapp.entity.Airline;
import com.flightapp.service.AirlineService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class AirControllerTest {
	@Mock
    private AirlineService airlineService;
    private WebTestClient client;
    private Airline sample;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        AirlineController controller = new AirlineController(airlineService);
        client = WebTestClient.bindToController(controller).build();
        sample = new Airline(UUID.randomUUID().toString(), "Indigo", "http://logo.com/a.png", List.of("F1", "F2")
        );
    }

    @Test
    void testGetAllAirlines() {
        when(airlineService.getAllAirlines()).thenReturn(Flux.just(sample));
        client.get().uri("/api/flight/airline/get").exchange().expectStatus().isOk().expectBody().jsonPath("$[0].name").isEqualTo("Indigo");
        verify(airlineService).getAllAirlines();
    }

    @Test
    void testGetById() {
        when(airlineService.getById(sample.getId())).thenReturn(Mono.just(sample));
        client.get().uri("/api/flight/airline/get/" + sample.getId()).exchange().expectStatus().isOk().expectBody()
        .jsonPath("$.logoUrl").isEqualTo("http://logo.com/a.png");
        verify(airlineService).getById(sample.getId());
    }

    @Test
    void testAddAirline() {
        when(airlineService.addAirline(any())).thenReturn(Mono.just(sample));
        client.post().uri("/api/flight/airline/add")
            .bodyValue(sample)
            .exchange()
            .expectStatus().isCreated()
            .expectBody().jsonPath("$.id").exists();
        verify(airlineService).addAirline(any());
    }

    @Test
    void testAddFlightToAirline() {
        when(airlineService.addFlightToAirline("A1", "F101")).thenReturn(Mono.just(sample));
        client.put().uri("/api/flight/airline/A1/add/F101").exchange().expectStatus().isOk();
        verify(airlineService).addFlightToAirline("A1", "F101");
    }
    
    @Test
    void testAddAirline_Duplicate() {
        when(airlineService.addAirline(any())).thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Airline exists")));
        client.post().uri("/api/flight/airline/add")
            .bodyValue(sample)
            .exchange()
            .expectStatus().isEqualTo(409) 
            .expectBody()
            .jsonPath("$.message").isEqualTo("Airline exists"); 
        verify(airlineService).addAirline(any());
    }
}
