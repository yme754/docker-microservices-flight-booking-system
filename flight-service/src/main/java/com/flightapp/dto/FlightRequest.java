package com.flightapp.dto;

import java.time.LocalDateTime;

import com.flightapp.entity.Price;

import lombok.Data;

@Data
public class FlightRequest {
	private String fromPlace;
    private String toPlace;
    private LocalDateTime arrivalTime;
    private LocalDateTime departureTime;
    private int availableSeats;
    private Price price;
    private String airlineId;
    private String flightNumber;
}
