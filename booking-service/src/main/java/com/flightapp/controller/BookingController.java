package com.flightapp.controller;

import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.dto.BookingDTO;
import com.flightapp.entity.Booking;
import com.flightapp.service.BookingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/flight/bookings")
public class BookingController {
	private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Map<String, String>> createBooking(@RequestBody BookingDTO bookingDTO) {
        return bookingService.createBooking(toEntity(bookingDTO)).map(booking -> Map.of("id", booking.getId()));
    }
    
    @PostMapping("/book")
    @ResponseStatus(HttpStatus.CREATED) // Sets status to 201
    public Mono<Map<String, String>> bookFlight(@RequestBody BookingDTO bookingDTO) {
        return bookingService.bookFlight(toEntity(bookingDTO)).map(booking -> Map.of("id", booking.getId()));
    }

    @GetMapping("/{pnr}")
    public Mono<BookingDTO> getByPnr(@PathVariable String pnr) {
        return bookingService.getBookingByPnr(pnr)
                .map(this::toDto);
    }

    @GetMapping
    public Flux<BookingDTO> getAll() {
        return bookingService.getAllBookings()
                .map(this::toDto);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable String id) {
        return bookingService.deleteBooking(id)
                .then(Mono.just(ResponseEntity.ok("Booking deleted successfully!")))
                .onErrorResume(ex -> {
                    String errorMsg = ex.getMessage();
                    if (errorMsg != null && errorMsg.contains("invalid flight"))
                        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body("invalid flight"));
                    return Mono.just(ResponseEntity.badRequest().body(errorMsg));
                });
    }
    private BookingDTO toDto(Booking booking) {
        BookingDTO dto = new BookingDTO();
        dto.setId(booking.getId());
        dto.setPnr(booking.getPnr());
        dto.setEmail(booking.getEmail());
        dto.setFlightId(booking.getFlightId());
        dto.setSeatCount(booking.getSeatCount());
        dto.setPassengerIds(booking.getPassengerIds());
        dto.setSeatNumbers(booking.getSeatNumbers());
        dto.setTotalAmount(Optional.ofNullable(booking.getTotalAmount()).orElse(0f));
        return dto;
    }

    private Booking toEntity(BookingDTO dto) {
        Booking booking = new Booking();
        booking.setId(dto.getId());
        booking.setPnr(dto.getPnr());
        booking.setEmail(dto.getEmail());
        booking.setFlightId(dto.getFlightId());
        booking.setSeatCount(dto.getSeatCount());
        booking.setPassengerIds(dto.getPassengerIds());
        booking.setSeatNumbers(dto.getSeatNumbers());
        booking.setTotalAmount(dto.getTotalAmount());
        return booking;
    }

}