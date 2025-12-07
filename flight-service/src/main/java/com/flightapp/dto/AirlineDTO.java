package com.flightapp.dto;

import java.util.List;

import lombok.Data;

@Data
public class AirlineDTO {
	private String id;
    private String name;
    private String country;
	private String logoUrl;
	private List<String> flightIds;
}
