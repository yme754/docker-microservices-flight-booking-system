package com.flightapp.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.flightapp.events.BookingCreatedEvent;
import com.flightapp.events.BookingCancelledEvent;
import com.flightapp.service.EmailSenderService;

@Service
public class BookingEventConsumer {
	private final EmailSenderService emailSenderService;

    public BookingEventConsumer(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @KafkaListener(topics = "booking-created", groupId = "emailGroup")
    public void consume(BookingCreatedEvent event) {
        System.out.println("Received Booking Created: " + event.getPnr());
        String subject = "Booking Confirmed - " + event.getPnr();
        String message = "Your booking for " + event.getSeatCount() + " seats is confirmed.\nPNR: " + event.getPnr() 
        +"\n\n Wishing you a safe and happy journey!";
        emailSenderService.sendEmail(event.getEmail(), subject, message);
    }

    @KafkaListener(topics = "booking-cancelled", groupId = "emailGroup")
    public void consumeBookingCancelled(BookingCancelledEvent event) {
        System.out.println("Received Booking Cancelled: " + event.getPnr());
        String subject = "Booking Cancelled - " + event.getPnr();
        String message = "Your flight " + event.getPnr() + " has been cancelled.\nReason: " + event.getReason();
        emailSenderService.sendEmail(event.getEmail(), subject, message);
    }
}