package com.flightapp.service.implementation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.flightapp.dto.FlightDTO;
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
    private final WebClient webClient;
    @Value("${flightapp.internal.jwt}")
    private String internalJwt;

    public BookingSImplementation(BookingRepository bookingRepo, BookingEventProducer bookingEventProducer, WebClient.Builder webClientBuilder) {
        this.bookingRepo = bookingRepo;
        this.bookingEventProducer = bookingEventProducer;
        this.webClient = webClientBuilder.baseUrl("http://flight-service:8082").build();
    }
    @Override
    @CircuitBreaker(name = "flightServiceBreaker", fallbackMethod = "bookFlightFallback")
    public Mono<Booking> bookFlight(Booking bookingRequest) {
    	return webClient.post()
    		    .uri("/api/flight/seats/{id}/book", bookingRequest.getFlightId())
    		    .header(HttpHeaders.AUTHORIZATION, "Bearer " + internalJwt)
    		    .bodyValue(bookingRequest.getSeatNumbers())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("Seat booking request invalid")
                                .flatMap(msg -> Mono.error(
                                        new IllegalArgumentException("Seat booking failed: " + msg)
                                )))
                .onStatus(HttpStatusCode::is5xxServerError, resp ->
                        resp.bodyToMono(String.class)
                                .defaultIfEmpty("Seat service error")
                                .flatMap(msg -> Mono.error(
                                        new IllegalStateException("Seat service error: " + msg)
                                )))
                .toBodilessEntity()
                .then(webClient.put()
                	    .uri("/api/flight/flights/{id}/inventory?add={delta}",
                	         bookingRequest.getFlightId(),
                	         -bookingRequest.getSeatCount())
                	    .header(HttpHeaders.AUTHORIZATION, "Bearer " + internalJwt)
                	    .retrieve().onStatus(HttpStatusCode::isError, resp ->
                                        resp.bodyToMono(String.class).defaultIfEmpty("Inventory update failed")
                                                .flatMap(msg -> Mono.error(new IllegalStateException("Inventory update failed: " + msg)
                                                ))).bodyToMono(FlightDTO.class)
                ).flatMap(flightDto -> {
                    bookingRequest.setId(UUID.randomUUID().toString());
                    bookingRequest.setPnr(
                            "PNR-" + bookingRequest.getId().substring(0, 6).toUpperCase());
                    bookingRequest.setBookingDate(LocalDateTime.now());
                    return bookingRepo.save(bookingRequest)
                            .switchIfEmpty(Mono.error(
                                    new IllegalStateException("Failed to save booking")));
                }).doOnSuccess(saved -> {
                    if (saved != null && saved.getPnr().startsWith("PNR-")) {
                        bookingEventProducer.sendBookingCreatedEvent(
                            new BookingCreatedEvent(
                                saved.getId(),
                                saved.getEmail(),
                                saved.getPnr(),
                                saved.getSeatCount()
                            ));
                    }});
    }

    @Override
    public Mono<Booking> createBooking(Booking booking) {
        booking.setId(UUID.randomUUID().toString());
        booking.setPnr("PNR-" + booking.getId().substring(0, 6).toUpperCase());
        booking.setBookingDate(LocalDateTime.now());
        return bookingRepo.save(booking);
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
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid booking ID")))
                .flatMap(booking -> {
                    long hours = java.time.Duration
                            .between(booking.getBookingDate(), LocalDateTime.now())
                            .toHours();

                    if (hours > 24) return Mono.error(new RuntimeException("can't cancel flight after 24 hrs from booking"));
                    BookingCancelledEvent event = new BookingCancelledEvent();
                    event.setBookingId(booking.getId());
                    event.setEmail(booking.getEmail());
                    event.setPnr(booking.getPnr());
                    event.setSeatCount(booking.getSeatCount());
                    event.setReason("Cancelled by user");
                    bookingEventProducer.sendBookingCancelledEvent(event);
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

    public Mono<Booking> bookFlightFallback(Booking bookingRequest, Throwable ex) {
        Booking failed = new Booking();
        failed.setId(UUID.randomUUID().toString());
        failed.setEmail(bookingRequest.getEmail());
        failed.setFlightId(bookingRequest.getFlightId());
        failed.setSeatCount(bookingRequest.getSeatCount());
        failed.setPassengerIds(bookingRequest.getPassengerIds());
        failed.setSeatNumbers(bookingRequest.getSeatNumbers());
        failed.setBookingDate(LocalDateTime.now());
        failed.setPnr("FAILED-" + UUID.randomUUID().toString().substring(0, 6));
        return Mono.just(failed);
    }
}
