package com.timcritt.tfg.infrastructure.messaging;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Test;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.util.function.BiFunction;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class StorageCleanupKafkaConfigurationContractTest {

	private static final String CONFIG_CLASS_NAME =
			"com.timcritt.tfg.infrastructure.messaging.StorageCleanupKafkaConfiguration";
	private static final String FACTORY_BEAN_NAME = "storageCleanupKafkaListenerContainerFactory";
	private static final String ERROR_HANDLER_BEAN_NAME = "storageCleanupKafkaErrorHandler";
	private static final String BACK_OFF_BEAN_NAME = "storageCleanupKafkaBackOff";
	private static final String RECOVERER_BEAN_NAME = "storageCleanupDeadLetterPublishingRecoverer";
	private static final String DESTINATION_RESOLVER_BEAN_NAME = "storageCleanupDeadLetterDestinationResolver";

	@Test
	void dedicatedStorageCleanupKafkaConfiguration_definesRetryBackoffAndDltContract() {
		new ApplicationContextRunner()
				.withUserConfiguration(storageCleanupKafkaConfigurationClass())
				.withBean(ConcurrentKafkaListenerContainerFactoryConfigurer.class,
						() -> mock(ConcurrentKafkaListenerContainerFactoryConfigurer.class))
				.withBean(ConsumerFactory.class, () -> mock(ConsumerFactory.class))
				.withBean(KafkaOperations.class, () -> mock(KafkaOperations.class))
				.run(context -> {
					assertThat(context).hasBean(FACTORY_BEAN_NAME);
					assertThat(context).hasBean(ERROR_HANDLER_BEAN_NAME);
					assertThat(context).hasBean(BACK_OFF_BEAN_NAME);
					assertThat(context).hasBean(RECOVERER_BEAN_NAME);
					assertThat(context).hasBean(DESTINATION_RESOLVER_BEAN_NAME);

					assertThat(context.getBean(FACTORY_BEAN_NAME))
							.isInstanceOf(ConcurrentKafkaListenerContainerFactory.class);
					assertThat(context.getBean(ERROR_HANDLER_BEAN_NAME))
							.isInstanceOf(DefaultErrorHandler.class);
					assertThat(context.getBean(RECOVERER_BEAN_NAME))
							.isInstanceOf(DeadLetterPublishingRecoverer.class);

					FixedBackOff backOff = context.getBean(BACK_OFF_BEAN_NAME, FixedBackOff.class);
					assertThat(backOff.getInterval()).isEqualTo(5_000L);
					assertThat(backOff.getMaxAttempts()).isEqualTo(5L);

					assertThat(context.getBean(FACTORY_BEAN_NAME, ConcurrentKafkaListenerContainerFactory.class)).isNotNull();
					assertThat(context.getBean(ERROR_HANDLER_BEAN_NAME, DefaultErrorHandler.class)).isNotNull();

					@SuppressWarnings("unchecked")
					BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition> destinationResolver =
							(BiFunction<ConsumerRecord<?, ?>, Exception, TopicPartition>) context.getBean(DESTINATION_RESOLVER_BEAN_NAME);

					TopicPartition destination = destinationResolver.apply(
							new ConsumerRecord<>("storage.object.delete.requested.v1", 2, 0L, "91", "payload"),
							new RuntimeException("minio down")
					);

					assertThat(destination.topic()).isEqualTo("storage.object.delete.requested.v1.DLT");
					assertThat(destination.partition()).isEqualTo(2);
				});
	}

	private static Class<?> storageCleanupKafkaConfigurationClass() {
		try {
			return Class.forName(CONFIG_CLASS_NAME);
		} catch (ClassNotFoundException ex) {
			throw new AssertionError(
					"Expected dedicated Kafka retry/DLT configuration class to exist: " + CONFIG_CLASS_NAME,
					ex
			);
		}
	}
}

