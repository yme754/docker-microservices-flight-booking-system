package com.flightapp.service.implementation;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.flightapp.entity.Booking;
import com.flightapp.events.BookingCancelledEvent;
import com.flightapp.events.BookingCreatedEvent;
import com.flightapp.kafka.BookingEventProducer;
import com.flightapp.repository.BookingRepository;
import com.flightapp.service.BookingService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class BookingSImplementation implements BookingService {

    private final BookingRepository bookingRepo;
    private final BookingEventProducer bookingEventProducer;

    public BookingSImplementation(BookingRepository bookingRepo, BookingEventProducer bookingEventProducer) {
        this.bookingRepo = bookingRepo;
        this.bookingEventProducer = bookingEventProducer;
    }

    @Override
    public Mono<Booking> createBooking(Booking booking) {
        booking.setId(UUID.randomUUID().toString());
        booking.setPnr("PNR-" + booking.getId().substring(0, 6).toUpperCase());
        booking.setBookingDate(LocalDateTime.now());
        return bookingRepo.save(booking);
    }

    @Override
    @CircuitBreaker(name = "flightServiceBreaker", fallbackMethod = "bookFlightFallback")
    public Mono<Booking> bookFlight(Booking bookingRequest) {
        int dummyAvailableSeats = 100;
        if (bookingRequest.getSeatCount() > dummyAvailableSeats) return Mono.error(new RuntimeException("Not enough seats available"));
        bookingRequest.setId(UUID.randomUUID().toString());
        bookingRequest.setPnr("PNR-" + bookingRequest.getId().substring(0, 6).toUpperCase());
        bookingRequest.setBookingDate(LocalDateTime.now());
        return bookingRepo.save(bookingRequest)
                .doOnSuccess(saved ->bookingEventProducer.sendBookingCreatedEvent(
                                new BookingCreatedEvent(
                                        saved.getId(),
                                        saved.getEmail(),
                                        saved.getPnr(),
                                        saved.getSeatCount())));
    }


    @Override
    public Mono<Booking> getBookingByPnr(String pnr) {
        return bookingRepo.findAll()
                .filter(b -> pnr.equals(b.getPnr()))
                .singleOrEmpty();
    }

    @Override
    public Flux<Booking> getAllBookings() {
        return bookingRepo.findAll();
    }

    @Override
    public Mono<Void> deleteBooking(String id) {
    	return bookingRepo.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid flight ID"))) 
                .flatMap(booking -> {
                    long hoursSinceBooking = Duration.between(booking.getBookingDate(), LocalDateTime.now()).toHours();
                    if (hoursSinceBooking > 24) return Mono.error(new RuntimeException("can't cancel flight after 24 hrs from booking"));
                    return bookingRepo.delete(booking);
                });
    }




    @Override
    public Mono<Booking> updateSeatNumbers(String bookingId, List<String> seatNumbers) {
        return bookingRepo.findById(bookingId)
                .flatMap(b -> {
                    b.setSeatNumbers(seatNumbers);
                    return bookingRepo.save(b);
                });
    }

    @Override
    public Mono<Booking> updatePassengerIds(String bookingId, List<String> passengerIds) {
        return bookingRepo.findById(bookingId)
                .flatMap(b -> {
                    b.setPassengerIds(passengerIds);
                    return bookingRepo.save(b);
                });
    }

    @Override
    public Mono<Booking> updateTotalAmount(String bookingId, float amount) {
        return bookingRepo.findById(bookingId)
                .flatMap(b -> {
                    b.setTotalAmount(amount);
                    return bookingRepo.save(b);
                });
    }

    public Mono<Booking> bookFlightFallback(Booking bookingRequest) {
        Booking failedBooking = new Booking();
        failedBooking.setEmail(bookingRequest.getEmail());
        failedBooking.setFlightId(bookingRequest.getFlightId());
        failedBooking.setSeatCount(bookingRequest.getSeatCount());
        failedBooking.setPassengerIds(bookingRequest.getPassengerIds());
        failedBooking.setSeatNumbers(bookingRequest.getSeatNumbers());
        failedBooking.setPnr("FAILED-" + UUID.randomUUID().toString().substring(0, 6));
        return Mono.just(failedBooking);
    }
}
