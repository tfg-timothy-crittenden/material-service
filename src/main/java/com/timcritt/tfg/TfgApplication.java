package com.timcritt.tfg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.timcritt.tfg.infrastructure.messaging.MessagingKafkaProperties;

@EnableKafka
@EnableConfigurationProperties(MessagingKafkaProperties.class)
@EnableScheduling
@SpringBootApplication
public class TfgApplication {

	@SuppressWarnings("unused")
	private static final Class<MessagingKafkaProperties> MESSAGING_KAFKA_PROPERTIES = MessagingKafkaProperties.class;

	public static void main(String[] args) {
		SpringApplication.run(TfgApplication.class, args);
	}

}
