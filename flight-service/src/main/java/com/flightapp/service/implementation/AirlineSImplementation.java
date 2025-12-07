package com.flightapp.service.implementation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.flightapp.entity.Airline;
import com.flightapp.repository.AirlineRepository;
import com.flightapp.service.AirlineService;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AirlineSImplementation implements AirlineService{
	private final AirlineRepository airlineRepo;

    @Override
    public Flux<Airline> getAllAirlines() {
        return airlineRepo.findAll();
    }

    @Override
    public Mono<Airline> getById(String id) {
        return airlineRepo.findById(id);
    }

    @Override
    public Mono<Airline> addAirline(Airline airline) {
        return airlineRepo.findByName(airline.getName())
            .flatMap(existingAirline -> 
                Mono.<Airline>error(new ResponseStatusException(HttpStatus.CONFLICT, "Airline with name '" + airline.getName() + "' already exists")))
            .switchIfEmpty(airlineRepo.save(airline));
    }

    @Override
    public Mono<Airline> addFlightToAirline(String airlineId, String flightId) {
        return airlineRepo.findById(airlineId).flatMap(airline -> {
            airline.getFlightIds().add(flightId);
            return airlineRepo.save(airline);
        });
    }
}
