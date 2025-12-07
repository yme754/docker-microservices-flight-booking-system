package com.flightapp.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.flightapp.events.BookingCancelledEvent;
import com.flightapp.events.BookingCreatedEvent;

@Component
public class BookingEventProducer {
	private final KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate;
    public BookingEventProducer(KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate) {
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
		// TODO Auto-generated method stub
		
	}
}