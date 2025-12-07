package com.flightapp.entity;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Price {
	@Positive(message = "oneWay trip price must be > 0")
	private float oneWay;
	@Positive(message = "roundTrip trip price must be > 0")
	private float roundTrip;
}
