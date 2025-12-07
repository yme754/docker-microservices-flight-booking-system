package com.flightapp.dto;

import lombok.Data;

@Data
public class FlightDTO {
	private String id;
    private String from;
    private String to;
    private int availableSeats;
    private double price;
}
