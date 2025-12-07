package com.flightapp.dto;

import lombok.Data;

@Data
public class SeatDTO {
	private String id;
    private String seatNumber;
    private boolean available;
    private String flightId;
}
