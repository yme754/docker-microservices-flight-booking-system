package com.flightapp.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingCancelledEvent {
	private String bookingId;
    private String email;
    private String pnr;
    private int seatCount;
    private String reason;
}
