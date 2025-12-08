package com.flightapp.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.flightapp.dto.AirlineRequest;
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
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')") 
    public Flux<Airline> getAllAirlines() {
        return airlineService.getAllAirlines();
    }

    @GetMapping("/get/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN', 'ROLE_USER')") 
    public Mono<Airline> getById(@PathVariable String id) {
        return airlineService.getById(id);
    }

    @PostMapping("/add")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')") 
    public Mono<ResponseEntity<Object>> addAirline(@RequestBody AirlineRequest airlineRequest) {
        Airline airline = new Airline();
        airline.setName(airlineRequest.getName());
        airline.setLogoUrl(airlineRequest.getLogoUrl());
        airline.setFlightIds(airlineRequest.getFlightIds());
        return airlineService.addAirline(airline)
            .map(savedAirline -> ResponseEntity.status(HttpStatus.CREATED).body((Object)savedAirline))
            .onErrorResume(ResponseStatusException.class, ex -> {
                Map<String, String> errorResponse = Map.of("message", ex.getReason());
                return Mono.just(ResponseEntity.status(ex.getStatusCode()).body(errorResponse));
            });
    }

    @PutMapping("/{airlineId}/add/{flightId}")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN')") 
    public Mono<Airline> addFlightToAirline(
            @PathVariable String airlineId,
            @PathVariable String flightId) {
        return airlineService.addFlightToAirline(airlineId, flightId);
    }
}
