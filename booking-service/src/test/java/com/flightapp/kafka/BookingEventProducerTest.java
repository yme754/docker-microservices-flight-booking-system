package com.flightapp.kafka;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import com.flightapp.events.BookingCreatedEvent;

class BookingEventProducerTest {
	@Mock
    private KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate;

    @InjectMocks
    private BookingEventProducer bookingEventProducer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendBookingCreatedEvent() {
        BookingCreatedEvent event = new BookingCreatedEvent("1", "mail@test.com", "PNR123", 2);
        CompletableFuture<SendResult<String, BookingCreatedEvent>> future = CompletableFuture.completedFuture(null);
        when(kafkaTemplate.send("booking-created", event.getBookingId(), event)).thenReturn(future);
        bookingEventProducer.sendBookingCreatedEvent(event);
        verify(kafkaTemplate, times(1)).send("booking-created", event.getBookingId(), event);
    }

}