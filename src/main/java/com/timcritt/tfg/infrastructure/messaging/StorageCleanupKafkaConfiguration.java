package com.timcritt.tfg.infrastructure.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.function.BiFunction;

@Configuration
public class StorageCleanupKafkaConfiguration {

    private static final long RETRY_INTERVAL_MILLIS = 5_000L;
    private static final long RETRY_ATTEMPTS = 5L;
    private static final String DEAD_LETTER_SUFFIX = ".DLT";

    @Bean("storageCleanupKafkaBackOff")
    FixedBackOff storageCleanupKafkaBackOff() {
        return new FixedBackOff(RETRY_INTERVAL_MILLIS, RETRY_ATTEMPTS);
    }

    @Bean("storageCleanupDeadLetterDestinationResolver")
    BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> storageCleanupDeadLetterDestinationResolver() {
        return (record, exception) -> new TopicPartition(
                record.topic() + DEAD_LETTER_SUFFIX,
                record.partition()
        );
    }

    @Bean("storageCleanupDeadLetterPublishingRecoverer")
    DeadLetterPublishingRecoverer storageCleanupDeadLetterPublishingRecoverer(
            KafkaOperations<Object, Object> kafkaOperations,
            BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> storageCleanupDeadLetterDestinationResolver
    ) {
        return new DeadLetterPublishingRecoverer(
                kafkaOperations,
                storageCleanupDeadLetterDestinationResolver::apply
        );
    }

    @Bean("storageCleanupKafkaErrorHandler")
    DefaultErrorHandler storageCleanupKafkaErrorHandler(
            DeadLetterPublishingRecoverer storageCleanupDeadLetterPublishingRecoverer,
            FixedBackOff storageCleanupKafkaBackOff
    ) {
        return new DefaultErrorHandler(
                storageCleanupDeadLetterPublishingRecoverer,
                storageCleanupKafkaBackOff
        );
    }

    @Bean("storageCleanupKafkaListenerContainerFactory")
    @SuppressWarnings({"rawtypes", "unchecked"})
    ConcurrentKafkaListenerContainerFactory<String, String> storageCleanupKafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ConsumerFactory<String, String> consumerFactory,
            DefaultErrorHandler storageCleanupKafkaErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<String, String> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure((ConcurrentKafkaListenerContainerFactory) factory, (ConsumerFactory) consumerFactory);
        factory.setCommonErrorHandler(storageCleanupKafkaErrorHandler);
        return factory;
    }
}

