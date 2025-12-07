package com.flightapp.kafka;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import com.flightapp.events.BookingCreatedEvent;

class KafkaProducerConfigTest {

    private final KafkaProducerConfig config = new KafkaProducerConfig();

    @Test
    void testProducerFactoryBean() {
        ProducerFactory<String, BookingCreatedEvent> factory = config.producerFactory();
        assertNotNull(factory);
    }

    @Test
    void testKafkaTemplateBean() {
        KafkaTemplate<String, BookingCreatedEvent> template = config.kafkaTemplate();
        assertNotNull(template);
    }
}