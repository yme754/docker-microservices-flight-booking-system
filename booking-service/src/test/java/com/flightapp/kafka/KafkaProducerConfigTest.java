package com.flightapp.kafka;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.util.ReflectionTestUtils;

class KafkaProducerConfigTest {

	private final KafkaProducerConfig config = new KafkaProducerConfig();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(config, "bootstrapServers", "localhost:9092");
    }

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