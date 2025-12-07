package com.flightapp.kafka;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

class KafkaProducerConfigTest {

    private final KafkaProducerConfig config = new KafkaProducerConfig();

    @Test
    void testProducerFactoryBean() {
        ProducerFactory<String, Object> factory = config.producerFactory();
        assertNotNull(factory);
    }

    @Test
    void testKafkaTemplateBean() {
        KafkaTemplate<String, Object> template = config.kafkaTemplate();
        assertNotNull(template);
    }
}