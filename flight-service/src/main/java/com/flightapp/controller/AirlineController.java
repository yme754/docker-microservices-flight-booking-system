package com.flightapp.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.entity.Airline;
import com.flightapp.service.AirlineService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/flight/airline")
public class AirlineController {
	private final AirlineService airlineService;
	
	public AirlineController(AirlineService airlineService) {
        this.airlineService = airlineService;
    }
	@GetMapping("/get")
    public Flux<Airline> getAllAirlines() {
        return airlineService.getAllAirlines();
    }

    @GetMapping("/get/{id}")
    public Mono<Airline> getById(@PathVariable String id) {
        return airlineService.getById(id);
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Map<String, String>> addAirline(@RequestBody Airline airline) {
        return airlineService.addAirline(airline).map(savedAirline -> Map.of("id", savedAirline.getId()));
    }

    @PutMapping("/{airlineId}/add-flight/{flightId}")
    public Mono<Airline> addFlightToAirline(
            @PathVariable String airlineId,
            @PathVariable String flightId) {
        return airlineService.addFlightToAirline(airlineId, flightId);
    }
}
