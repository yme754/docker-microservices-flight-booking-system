package com.flightapp.feign;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.flightapp.dto.FlightDTO;
import com.flightapp.dto.SeatDTO;


@FeignClient(name = "flight-service")
public interface FlightClient {
	
	@GetMapping("/api/flight/flights/{flightId}")
    FlightDTO getFlightById(@PathVariable String flightId);

    @PutMapping("/api/flight/flights/{id}")
    void updateFlight(@PathVariable String id, @RequestBody FlightDTO flight);

    @GetMapping("/api/flight/seats/{flightId}")
    List<SeatDTO> getSeatsByFlightId(@PathVariable String flightId);

    @PutMapping("/api/flight/seats/{id}/update")
    void updateSeats(@PathVariable String id, @RequestBody List<SeatDTO> seats);
}