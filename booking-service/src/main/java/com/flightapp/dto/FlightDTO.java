package com.flightapp.dto;

import lombok.Data;

@Data
public class FlightDTO {
	private String id;
    private String flightNumber;
    private int availableSeats;
}
