package com.flightapp.service.implementation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.annotation.Import;

import com.flightapp.config.Resilience4jTestConfig;
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

    @InjectMocks
    private BookingSImplementation bookingService;

    private Booking booking;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        booking = new Booking();
        booking.setId(UUID.randomUUID().toString());
        booking.setFlightId("FL123");
        booking.setSeatCount(2);
        booking.setPassengerIds(List.of("P1","P2"));
        booking.setSeatNumbers(List.of("1A","1B"));
        booking.setEmail("test@example.com");
        booking.setBookingDate(LocalDateTime.now()); 
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
    }

    @Test
    void testBookFlight_NotEnoughSeats() {
        booking.setSeatCount(101);

        StepVerifier.create(bookingService.bookFlight(booking))
                .expectErrorMatches(ex -> ex.getMessage().equals("Not enough seats available"))
                .verify();

        verify(bookingRepo, never()).save(any());
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
        StepVerifier.create(bookingService.updateSeatNumbers("1", Arrays.asList("2A","2B")))
                .expectNext(booking)
                .verifyComplete();
    }

    @Test
    void testUpdatePassengerIds() {
        when(bookingRepo.findById("1")).thenReturn(Mono.just(booking));
        when(bookingRepo.save(any())).thenReturn(Mono.just(booking));
        StepVerifier.create(bookingService.updatePassengerIds("1", Arrays.asList("PX1","PX2"))).expectNext(booking).verifyComplete();
    }

    @Test
    void testUpdateTotalAmount() {
        when(bookingRepo.findById("1")).thenReturn(Mono.just(booking));
        when(bookingRepo.save(any())).thenReturn(Mono.just(booking));
        StepVerifier.create(bookingService.updateTotalAmount("1", 5000))
                .expectNext(booking)
                .verifyComplete();
    }
}