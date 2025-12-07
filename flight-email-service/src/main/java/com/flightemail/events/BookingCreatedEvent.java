package com.flightemail.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCreatedEvent {
	private String bookingId;
    private String email;
    private String pnr;
    private int seatCount;
}
