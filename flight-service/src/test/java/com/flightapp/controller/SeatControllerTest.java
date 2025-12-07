package com.flightapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightapp.entity.Seat;
import com.flightapp.service.SeatService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class SeatControllerTest {
	@Mock
    private SeatService seatService;
    private WebTestClient client;
    private Seat seat;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        SeatController controller = new SeatController(seatService);
        client = WebTestClient.bindToController(controller).build();
        seat = new Seat("S1", "1A", true, "F101");
    }

    @Test
    void testGetSeatsByFlight() {
        when(seatService.getSeatsByFlightId("F101")).thenReturn(Flux.just(seat));
        client.get().uri("/api/flight/seats/F101").exchange().expectStatus().isOk()
         .expectBody().jsonPath("$[0].seatNumber").isEqualTo("1A");
        verify(seatService).getSeatsByFlightId("F101");
    }

    @Test
    void testUpdateSeats() {
        when(seatService.updateSeats("F101", List.of(seat))).thenReturn(Mono.empty());
        client.put().uri("/api/flight/seats/F101/update").bodyValue(List.of(seat)).exchange().expectStatus().isOk();
        verify(seatService).updateSeats("F101", List.of(seat));
    }
    
    @Test
    void testAddNewSeats() {
        when(seatService.addSeats(any(), any())).thenReturn(Mono.empty());
        client.post().uri("/api/flight/seats/F101/add")
            .bodyValue(List.of(seat))
            .exchange()
            .expectStatus().isOk();
        verify(seatService).addSeats(any(), any());
    }
}
