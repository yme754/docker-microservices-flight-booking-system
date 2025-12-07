package com.flightapp.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.flightapp.events.BookingCancelledEvent;
import com.flightapp.events.BookingCreatedEvent;

@Component
public class BookingEventProducer {
	private final KafkaTemplate<String, Object> kafkaTemplate;
    public BookingEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void sendBookingCreatedEvent(BookingCreatedEvent event) {
        kafkaTemplate.send("booking-created", event.getBookingId(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Booking event published: " + event);
                } else {
                    System.err.println("Failed to publish booking event: " + ex.getMessage());
                }
            });
    }

    public void sendBookingCancelledEvent(BookingCancelledEvent event) {
        kafkaTemplate.send("booking-cancelled", event.getBookingId(), event)
            .whenComplete((result, ex) -> {
                if (ex == null) {
                    System.out.println("Cancellation event published: " + event.getPnr());
                } else {
                    System.err.println("Failed to publish cancellation: " + ex.getMessage());
                }
            });
    }
}