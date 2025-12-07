package com.flightapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightapp.dto.SearchRequestDTO;
import com.flightapp.entity.Flight;
import com.flightapp.entity.Price;
import com.flightapp.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class FlightControllerTest {
	@Mock
    private FlightService flightService;
    private WebTestClient client;
    private Flight flight;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        FlightController controller = new FlightController(flightService);
        client = WebTestClient.bindToController(controller).build();
        flight = new Flight(UUID.randomUUID().toString(), "HYD", "DEL", LocalDateTime.now().plusDays(1),
        LocalDateTime.now().plusDays(1).plusHours(2), 100, new Price(2000, 3500), "A1", "6E101"
        );
    }

    @Test
    void testGetFlightById() {
        when(flightService.getFlightById(flight.getId())).thenReturn(Mono.just(flight));
        client.get().uri("/api/flight/flights/" + flight.getId()).exchange().expectStatus().isOk()
        .expectBody().jsonPath("$.fromPlace").isEqualTo("HYD");
        verify(flightService).getFlightById(flight.getId());
    }

    @Test
    void testAddFlight() {
        when(flightService.addFlight(any())).thenReturn(Mono.just(flight));
        client.post().uri("/api/flight/flights/add")
            .bodyValue(flight)
            .exchange()
            .expectStatus().isCreated()
            .expectBody().jsonPath("$.id").exists();
        verify(flightService).addFlight(any());
    }

    @Test
    void testSearchFlights() {
        SearchRequestDTO searchReq = new SearchRequestDTO();
        searchReq.setFrom("HYD");
        searchReq.setTo("DEL");
        when(flightService.searchFlights("HYD", "DEL")).thenReturn(Flux.just(flight));        
        client.post().uri("/api/flight/flights/search") 
            .bodyValue(searchReq)
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$[0].airlineId").isEqualTo("A1");
        
        verify(flightService).searchFlights("HYD", "DEL");
    }

    @Test
    void testIncreaseInventory() {
        when(flightService.increaseAvailableSeats(flight.getId(), 10)).thenReturn(Mono.just(flight));
        client.put().uri("/api/flight/flights/" + flight.getId() + "/inventory?add=10").exchange().expectStatus().isOk();
        verify(flightService).increaseAvailableSeats(flight.getId(), 10);
    }
    

    @Test
    void testAddFlight_Duplicate() {
        when(flightService.addFlight(any())).thenReturn(Mono.error(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.CONFLICT, "Flight exists")));
        client.post().uri("/api/flight/flights/add")
            .bodyValue(flight)
            .exchange()
            .expectStatus().isEqualTo(409)
            .expectBody()
            .jsonPath("$.message").isEqualTo("Flight exists");
        verify(flightService).addFlight(any());
    }

    @Test
    void testGetAllFlights() {
        when(flightService.getAllFlights()).thenReturn(Flux.just(flight));
        client.get().uri("/api/flight/flights")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].flightNumber").isEqualTo("6E101");
        verify(flightService).getAllFlights();
    }
}
