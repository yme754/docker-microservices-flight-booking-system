package com.flightemail.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.flightapp.events.BookingCreatedEvent;
import com.flightemail.service.EmailSenderService;

@Service
public class BookingEventConsumer {
	private final EmailSenderService emailSenderService;

    public BookingEventConsumer(EmailSenderService emailSenderService) {
        this.emailSenderService = emailSenderService;
    }

    @KafkaListener(topics = "booking-created", groupId = "emailGroup")
    public void consume(BookingCreatedEvent event) {
        System.out.println("Received event: " + event);
        String subject = "Your flight booking is confirmed - " + event.getPnr();
        String message =
                "Thanks for booking your flight!\n\n" +
                "Your PNR is: " + event.getPnr() + "\n" +
                "Seats Booked: " + event.getSeatCount() + "\n\n" +
                "We wish you a pleasant journey!\n";
        emailSenderService.sendEmail(event.getEmail(), subject, message);
    }
    
    @KafkaListener(topics = "booking-cancelled", groupId = "emailGroup")
    public void consumeBookingCancelled(BookingCreatedEvent event) {
        System.out.println("Received Booking Cancelled event: " + event);
        String subject = "Your flight booking is cancelled - " + event.getPnr();
        String message =
                "Hello,\n\n" +
                "Your flight with PNR " + event.getPnr() + " has been cancelled successfully.\n\n";
        emailSenderService.sendEmail(event.getEmail(), subject, message);
    }
}