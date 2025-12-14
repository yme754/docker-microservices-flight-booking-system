package com.flightapp.service.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import com.flightapp.config.Resilience4jTestConfig;
import com.flightapp.dto.FlightDTO;
import com.flightapp.entity.Booking;
import com.flightapp.kafka.BookingEventProducer;
import com.flightapp.repository.BookingRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Import(Resilience4jTestConfig.class)
class BookingSImplementationTest {

    @Mock private WebClient.Builder webClientBuilder;
    @Mock private WebClient webClient;

    @Mock private WebClient.RequestBodyUriSpec postUriSpec;
    @Mock private WebClient.RequestBodySpec postBodySpec;
    @SuppressWarnings("rawtypes")
    @Mock private WebClient.RequestHeadersSpec postHeadersSpec;
    @Mock private WebClient.ResponseSpec postResponseSpec;

    @Mock private WebClient.RequestBodyUriSpec putUriSpec;
    @Mock private WebClient.RequestBodySpec putBodySpec;
    @Mock private WebClient.RequestHeadersSpec<?> putHeadersSpec;
    @Mock private WebClient.ResponseSpec putResponseSpec;

    @Mock private BookingRepository bookingRepo;
    @Mock private BookingEventProducer bookingEventProducer;

    private BookingSImplementation bookingService;

    private Booking booking;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        webClientBuilder = mock(WebClient.Builder.class);
        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setFlightId("FL123");
        booking.setSeatCount(1);
        booking.setPassengerIds(List.of("P1"));
        booking.setSeatNumbers(List.of("1B"));
        booking.setEmail("test@example.com");
        booking.setBookingDate(LocalDateTime.now());
        bookingService = new BookingSImplementation(bookingRepo, bookingEventProducer, webClientBuilder);
        doNothing().when(bookingEventProducer).sendBookingCreatedEvent(any());
        doNothing().when(bookingEventProducer).sendBookingCancelledEvent(any());

        when(webClient.post()).thenReturn(postUriSpec);
        when(postUriSpec.uri(anyString(), any(Object[].class))).thenReturn(postBodySpec);
        when(postBodySpec.header(anyString(), anyString())).thenReturn(postBodySpec);
        when(postBodySpec.bodyValue(any())).thenReturn(postHeadersSpec);
        when(postHeadersSpec.retrieve()).thenReturn(postResponseSpec);
        when(postResponseSpec.onStatus(any(), any())).thenReturn(postResponseSpec);
        when(postResponseSpec.toBodilessEntity()).thenReturn(Mono.just(ResponseEntity.ok().build()));

        when(webClient.put()).thenReturn(putUriSpec);
        when(putUriSpec.uri(anyString(), any(Object[].class))).thenReturn(putBodySpec);
        when(putBodySpec.header(anyString(), anyString())).thenReturn(putBodySpec);
        when(putBodySpec.retrieve()).thenReturn(putResponseSpec);
        when(putResponseSpec.onStatus(any(), any())).thenReturn(putResponseSpec);
        when(putResponseSpec.bodyToMono(FlightDTO.class)).thenReturn(Mono.just(new FlightDTO()));
    }

    @Test
    void testCreateBooking() {
        when(bookingRepo.save(any())).thenReturn(Mono.just(booking));
        StepVerifier.create(bookingService.createBooking(booking))
            .expectNextMatches(b -> b.getPnr() != null)
            .verifyComplete();
        verify(bookingRepo, times(1)).save(any());
    }

    @Test
    void testBookFlightSuccess() {
        when(bookingRepo.save(any())).thenReturn(Mono.just(booking));

        StepVerifier.create(bookingService.bookFlight(booking))
            .expectNextMatches(b -> b.getPnr() != null)
            .verifyComplete();

        verify(bookingRepo, times(1)).save(any());
        verify(webClient, times(1)).post();
        verify(webClient, times(1)).put();
    }

    @Test
    void testGetBookingByPnr() {
        booking.setPnr("PNR-TEST");
        when(bookingRepo.findAll()).thenReturn(Flux.just(booking));
        StepVerifier.create(bookingService.getBookingByPnr("PNR-TEST"))
            .expectNext(booking)
            .verifyComplete();
    }

    @Test
    void testGetAllBookings() {
        when(bookingRepo.findAll()).thenReturn(Flux.just(booking));
        StepVerifier.create(bookingService.getAllBookings())
            .expectNext(booking)
            .verifyComplete();
    }

    @Test
    void testDeleteBooking() {
        Booking existingBooking = new Booking();
        existingBooking.setId("123");
        existingBooking.setBookingDate(LocalDateTime.now());
        when(bookingRepo.findById("123")).thenReturn(Mono.just(existingBooking));
        when(bookingRepo.delete(existingBooking)).thenReturn(Mono.empty());
        StepVerifier.create(bookingService.deleteBooking("123")).verifyComplete();
        verify(bookingRepo, times(1)).delete(existingBooking);
    }

    @Test
    void testUpdateSeatNumbers() {
        when(bookingRepo.findById("1")).thenReturn(Mono.just(booking));
        when(bookingRepo.save(any())).thenReturn(Mono.just(booking));
        StepVerifier.create(bookingService.updateSeatNumbers("1", Arrays.asList("2A", "2B"))).expectNext(booking).verifyComplete();
    }

    @Test
    void testUpdatePassengerIds() {
        when(bookingRepo.findById("1")).thenReturn(Mono.just(booking));
        when(bookingRepo.save(any())).thenReturn(Mono.just(booking));
        StepVerifier.create(bookingService.updatePassengerIds("1", Arrays.asList("PX1", "PX2")))
            .expectNext(booking)
            .verifyComplete();
    }

    @Test
    void testUpdateTotalAmount() {
        when(bookingRepo.findById("1")).thenReturn(Mono.just(booking));
        when(bookingRepo.save(any())).thenReturn(Mono.just(booking));
        StepVerifier.create(bookingService.updateTotalAmount("1", 5000))
            .expectNext(booking)
            .verifyComplete();
    }

    @Test
    void testDeleteBooking_InvalidId() {
        when(bookingRepo.findById("invalid-id")).thenReturn(Mono.empty());
        StepVerifier.create(bookingService.deleteBooking("invalid-id"))
            .expectErrorMatches(ex -> ex.getMessage().contains("Invalid booking ID")).verify();
    }

    @Test
    void testDeleteBooking_TooLateToCancel() {
        Booking oldBooking = new Booking();
        oldBooking.setId("old123");
        oldBooking.setBookingDate(LocalDateTime.now().minusHours(25));
        when(bookingRepo.findById("old123")).thenReturn(Mono.just(oldBooking));
        StepVerifier.create(bookingService.deleteBooking("old123"))
            .expectErrorMatches(ex -> ex.getMessage().contains("can't cancel flight")).verify();
    }

    @Test
    void testBookFlightFallback() {
        Booking failedRequest = new Booking();
        failedRequest.setEmail("test@fail.com");
        failedRequest.setSeatCount(1);
        failedRequest.setFlightId("FL999");
        StepVerifier.create(
                bookingService.bookFlightFallback( failedRequest, new RuntimeException("Circuit breaker open")))
        .expectNextMatches(b -> b.getPnr().startsWith("FAILED")).verifyComplete();
    }

}
