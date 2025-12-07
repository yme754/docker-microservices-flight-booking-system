package com.flightapp.service.implementation;

import java.util.List;

import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.flightapp.entity.Flight;
import com.flightapp.entity.Seat;
import com.flightapp.repository.FlightRepository;
import com.flightapp.repository.SeatRepository;
import com.flightapp.service.FlightService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FlightSImplementation implements FlightService{
	private final FlightRepository flightRepo;
    private final SeatRepository seatRepo;
    private final ReactiveMongoTemplate mongoTemplate;
    
    public FlightSImplementation(FlightRepository flightRepo, SeatRepository seatRepo, ReactiveMongoTemplate mongoTemplate) {
        this.flightRepo = flightRepo;
        this.seatRepo = seatRepo;
        this.mongoTemplate = mongoTemplate;
    }
    
    @Override
    public Flux<Flight> getAllFlights() {
        return flightRepo.findAll();
    }

    @Override
    public Mono<Flight> getFlightById(String id) {
        return flightRepo.findById(id);
    }

    @Override
    public Mono<Void> updateFlight(String id, Flight flight) {
        flight.setId(id);
        return flightRepo.save(flight).then();
    }
    
    @Override
    public Mono<Flight> addFlight(Flight flight) {
        return flightRepo.findByFlightNumber(flight.getFlightNumber())
            .flatMap(existingFlight -> Mono.<Flight>error(new ResponseStatusException(HttpStatus.CONFLICT,
            		"Flight with number " + flight.getFlightNumber() + " already exists"))).switchIfEmpty(flightRepo.save(flight));
    }

    @Override
    public Flux<Flight> searchFlights(String from, String to) {
        return flightRepo.findByFromPlaceAndToPlace(from, to);
    }

    @Override
    public Flux<Seat> getSeatsByFlightId(String flightId) {
        return seatRepo.findByFlightId(flightId);
    }

    @Override
    public Mono<Void> updateSeats(String flightId, List<Seat> seats) {
        return seatRepo.findByFlightId(flightId).flatMap(seatRepo::delete)
                .thenMany(Flux.fromIterable(seats)).flatMap(seatRepo::save).then();
    }

    @Override
    public Mono<Flight> reduceAvailableSeats(String flightId, int seatCnt) {
        Query query = new Query(Criteria.where("id").is(flightId).and("availableSeats").gte(seatCnt));
        Update update = new Update().inc("availableSeats", -seatCnt); 
        return mongoTemplate.findAndModify(query, update, Flight.class).switchIfEmpty(Mono.error(new RuntimeException("Not enough seats available or Flight not found")));
    }

    @Override
    public Mono<Flight> increaseAvailableSeats(String flightId, int seatCount) {
        return flightRepo.findById(flightId)
                .flatMap(f -> {f.setAvailableSeats(f.getAvailableSeats() + seatCount);
                    return flightRepo.save(f);
                });
    }
}
