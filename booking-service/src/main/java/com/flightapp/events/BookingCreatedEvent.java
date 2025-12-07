package com.flightapp.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BookingCreatedEvent {
	private String bookingId;
    private String email;
    private String pnr;
    private int seatCount;
}