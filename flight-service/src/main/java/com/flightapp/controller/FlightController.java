package com.flightapp.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.flightapp.entity.Flight;
import com.flightapp.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/flight/flights")
public class FlightController {
	private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/{id}")
    public Mono<Flight> getFlightById(@PathVariable String id) {
        return flightService.getFlightById(id);
    }

    @PutMapping("/{id}")
    public Mono<Void> updateFlight(@PathVariable String id, @RequestBody Flight flight) {
        return flightService.updateFlight(id, flight);
    }

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Map<String, String>> addFlight(@RequestBody Flight flight) {
        return flightService.addFlight(flight).map(savedFlight -> Map.of("id", savedFlight.getId()));
    }

    @GetMapping("/search")
    public Flux<Flight> searchFlights(@RequestParam String from, @RequestParam String to) {
        return flightService.searchFlights(from, to);
    }

    @PutMapping("/{id}/inventory")
    public Mono<Flight> addInventory(@PathVariable String id, @RequestParam int add) {
        return flightService.increaseAvailableSeats(id, add);
    }
}
