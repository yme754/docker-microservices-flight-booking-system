package com.flightapp.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "flights")
public class Flight {
	@Id
	private String id;
	@NotBlank(message = "Source place is required")
	private String fromPlace;
	@NotBlank(message = "Destination place is required")
	private String toPlace;
	@Future(message = "Arrival time must be in the future")
	private LocalDateTime arrivalTime;
	@Future(message = "Departure time must be in the future")
	private LocalDateTime departureTime;
	@Positive(message = "Available seats must be positive")
	private int availableSeats;
	@NotNull(message = "Price details are required")
	private Price price;
	@NotBlank(message = "Airline ID is required")
	private String airlineId;
	private String flightNumber;

}
