package com.flightapp.service.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Import;
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
	@Mock
    private BookingRepository bookingRepo;

    @Mock
    private BookingEventProducer bookingEventProducer;
    @Mock
    private WebClient.Builder webClientBuilder;
    @Mock
    private WebClient webClient;
    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    private WebClient.RequestBodySpec requestBodySpec;
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private BookingSImplementation bookingService;

    private Booking booking;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setFlightId("FL123");
        booking.setSeatCount(2);
        booking.setPassengerIds(List.of("P1", "P2"));
        booking.setSeatNumbers(List.of("1A", "1B"));
        booking.setEmail("test@example.com");
        booking.setBookingDate(LocalDateTime.now());
        when(webClientBuilder.build()).thenReturn(webClient);
        when(webClient.post()).thenReturn(requestBodyUriSpec);
        when(webClient.put()).thenReturn(requestBodyUriSpec);        
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);        
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);        
        when(requestBodySpec.retrieve()).thenReturn(responseSpec); 
        when(responseSpec.onStatus(any(Predicate.class), any())).thenReturn(responseSpec);
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
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());
        when(responseSpec.bodyToMono(FlightDTO.class)).thenReturn(Mono.just(new FlightDTO()));        
        when(bookingRepo.save(any())).thenReturn(Mono.just(booking));
        StepVerifier.create(bookingService.bookFlight(booking)).expectNextMatches(b -> b.getPnr() != null).verifyComplete();
        verify(bookingRepo, times(1)).save(any());
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
        StepVerifier.create(bookingService.updateSeatNumbers("1", Arrays.asList("2A", "2B")))
                .expectNext(booking)
                .verifyComplete();
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
                .expectErrorMatches(ex -> ex.getMessage().contains("can't cancel flight"))
                .verify();
    }

    @Test
    void testBookFlightFallback() {
        Booking failedRequest = new Booking();
        failedRequest.setEmail("test@fail.com");
        failedRequest.setSeatCount(1);
        failedRequest.setFlightId("FL999");
        StepVerifier.create(bookingService.bookFlightFallback(failedRequest))
                .expectNextMatches(b -> b.getPnr().startsWith("FAILED"))
                .verifyComplete();
    }
    
}