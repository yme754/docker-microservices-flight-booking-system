package com.flightapp.dto;

import java.util.List;

import lombok.Data;

@Data
public class BookingDTO {
	private String id;
    private String pnr;
    private String email;
    private String flightId;
    private int seatCount;
    private List<String> passengerIds;
    private List<String> seatNumbers;
    private float totalAmount;
}
