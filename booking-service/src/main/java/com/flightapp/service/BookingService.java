package com.flightapp.service;

import java.util.List;

import com.flightapp.entity.Booking;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BookingService {
	Mono<Booking> createBooking(Booking booking);
    Mono<Booking> getBookingByPnr(String pnr);
    Flux<Booking> getAllBookings();
    Mono<Void> deleteBooking(String id);
    Mono<Booking> updateSeatNumbers(String bookingId, List<String> seatNumbers);
    Mono<Booking> updatePassengerIds(String bookingId, List<String> passengerIds);
    Mono<Booking> updateTotalAmount(String bookingId, float amount);
	Mono<Booking> bookFlight(Booking booking);

}