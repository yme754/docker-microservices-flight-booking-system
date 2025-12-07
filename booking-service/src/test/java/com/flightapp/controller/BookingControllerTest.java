package com.flightapp.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.flightapp.entity.Booking;
import com.flightapp.enums.GENDER;
import com.flightapp.enums.MEAL_PREFERENCE;
import com.flightapp.enums.TRIP_TYPE;
import com.flightapp.service.BookingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

class BookingControllerTest {
	private WebTestClient webTestClient;

    @Mock
    private BookingService bookingService;
    private Booking sampleBooking;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        BookingController bookingController = new BookingController(bookingService);
        webTestClient = WebTestClient.bindToController(bookingController).build();
        sampleBooking = new Booking();
        sampleBooking.setId(UUID.randomUUID().toString());
        sampleBooking.setPnr("PNR-ABC123");
        sampleBooking.setEmail("test@gmail.com");
        sampleBooking.setSeatCount(2);
        sampleBooking.setGender(GENDER.FEMALE);
        sampleBooking.setTripType(TRIP_TYPE.ONE_WAY);
        sampleBooking.setMealPref(MEAL_PREFERENCE.VEG);
        sampleBooking.setFlightId("FL123");
        sampleBooking.setPassengerIds(List.of("P1", "P2"));
        sampleBooking.setSeatNumbers(List.of("1A", "1B"));
        sampleBooking.setTotalAmount((float) 5000.0);
    }

    @Test
    void testCreateBooking() {
        when(bookingService.createBooking(any(Booking.class))).thenReturn(Mono.just(sampleBooking));        
        webTestClient.post().uri("/api/flight/bookings")
            .bodyValue(sampleBooking)
            .exchange()
            .expectStatus().isCreated() 
            .expectBody()
            .jsonPath("$.id").exists(); 
        verify(bookingService, times(1)).createBooking(any(Booking.class));
    }
    
    @Test
    void testGetBookingByPnr() {
        when(bookingService.getBookingByPnr("PNR-ABC123")).thenReturn(Mono.just(sampleBooking));        
        webTestClient.get().uri("/api/flight/bookings/PNR-ABC123")
            .exchange()
            .expectStatus().isOk()
            .expectBody().jsonPath("$.email").isEqualTo("test@gmail.com");
        verify(bookingService, times(1)).getBookingByPnr("PNR-ABC123");
    }
    
    @Test
    void testBookFlight() {
        when(bookingService.bookFlight(any(Booking.class))).thenReturn(Mono.just(sampleBooking));
        webTestClient.post().uri("/api/flight/bookings/book")
            .bodyValue(sampleBooking)
            .exchange()
            .expectStatus().isCreated() 
            .expectBody()
            .jsonPath("$.id").isEqualTo(sampleBooking.getId()); 
        verify(bookingService, times(1)).bookFlight(any(Booking.class));
    }

    @Test
    void testGetAllBookings() {
        when(bookingService.getAllBookings()).thenReturn(Flux.just(sampleBooking));
        webTestClient.get().uri("/api/flight/bookings")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$[0].pnr").isEqualTo("PNR-ABC123");
        verify(bookingService, times(1)).getAllBookings();
    }

    @Test
    void testDeleteBooking() {
        when(bookingService.deleteBooking("123")).thenReturn(Mono.empty());
        webTestClient.delete().uri("/api/flight/bookings/123")
            .exchange()
            .expectStatus().isOk();
        verify(bookingService, times(1)).deleteBooking("123");
    }
    
    @Test
    void testDeleteBooking_NotFound() {
        when(bookingService.deleteBooking("unknown")).thenReturn(Mono.error(new RuntimeException("invalid flight ID")));
        webTestClient.delete().uri("/api/flight/bookings/unknown")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody(String.class).isEqualTo("invalid flight"); 
    }

    @Test
    void testDeleteBooking_BadRequest() {
        when(bookingService.deleteBooking("old")).thenReturn(Mono.error(new RuntimeException("Too late")));
        webTestClient.delete().uri("/api/flight/bookings/old")
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody(String.class).isEqualTo("Too late");
    }
}