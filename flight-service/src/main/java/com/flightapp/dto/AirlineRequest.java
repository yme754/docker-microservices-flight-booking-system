package com.flightapp.dto;

import java.util.List;

import lombok.Data;

@Data
public class AirlineRequest {
	private String name;
    private String logoUrl;
    private List<String> flightIds;
}
