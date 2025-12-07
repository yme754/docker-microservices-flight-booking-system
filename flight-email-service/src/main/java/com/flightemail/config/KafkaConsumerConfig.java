package com.flightemail.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import com.flightapp.events.BookingCreatedEvent;

@Configuration
public class KafkaConsumerConfig {

	@SuppressWarnings("deprecation")
	@Bean
    public ConsumerFactory<String, BookingCreatedEvent> consumerFactory() {
        JsonDeserializer<BookingCreatedEvent> deserializer = new JsonDeserializer<>(BookingCreatedEvent.class);
        deserializer.addTrustedPackages("com.flightapp.events");
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "emailGroup");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), deserializer);
    }

    @Bean(name = "kafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, BookingCreatedEvent> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, BookingCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        return factory;
    }
}