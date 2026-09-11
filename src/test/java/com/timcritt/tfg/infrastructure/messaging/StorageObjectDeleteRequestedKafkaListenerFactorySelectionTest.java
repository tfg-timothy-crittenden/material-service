package com.timcritt.tfg.infrastructure.messaging;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.annotation.KafkaListener;

import static org.assertj.core.api.Assertions.assertThat;

class StorageObjectDeleteRequestedKafkaListenerFactorySelectionTest {

    @Test
    void storageCleanupListener_usesDedicatedKafkaListenerContainerFactory() throws Exception {
        KafkaListener kafkaListener = StorageObjectDeleteRequestedKafkaListener.class
                .getMethod("onMessage", String.class)
                .getAnnotation(KafkaListener.class);

        assertThat(kafkaListener).isNotNull();
        assertThat(kafkaListener.containerFactory())
                .isEqualTo("storageCleanupKafkaListenerContainerFactory");
    }

    @Test
    void materialDetailsListener_remainsOnDefaultKafkaListenerContainerFactory() throws Exception {
        KafkaListener kafkaListener = MaterialDetailsRequestedKafkaListener.class
                .getMethod("onMessage", String.class)
                .getAnnotation(KafkaListener.class);

        assertThat(kafkaListener).isNotNull();
        assertThat(kafkaListener.containerFactory()).isEmpty();
    }
}

