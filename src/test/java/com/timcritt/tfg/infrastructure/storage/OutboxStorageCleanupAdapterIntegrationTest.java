package com.timcritt.tfg.infrastructure.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.port.outbound.StorageRepositoryPort;
import com.timcritt.tfg.infrastructure.persistence.outbox.OutboxEventJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.outbox.OutboxEventJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.cloud.config.enabled=false",
        "eureka.client.enabled=false",
        "spring.kafka.listener.auto-startup=false",
        "material.kafka.outbox-relay.enabled=false",
        "authorization.classroom.enabled=false",
        "authorization.classroom.transport=http",
        "spring.docker.compose.enabled=false",
        "spring.flyway.enabled=true",
        "spring.flyway.baseline-on-migrate=true",
        "spring.flyway.baseline-version=0.0",
        "spring.flyway.locations=classpath:db/migration",
        "spring.sql.init.mode=never",
        "spring.jpa.hibernate.ddl-auto=none"
})
class OutboxStorageCleanupAdapterIntegrationTest {

    private static final String STORAGE_OBJECT_DELETE_REQUESTED = "storage.object.delete.requested.v1";

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("material_db")
            .withUsername("myuser")
            .withPassword("secret");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private OutboxStorageCleanupAdapter storageCleanupAdapter;

    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StorageRepositoryPort storageRepositoryPort;

    @BeforeEach
    void clearOutbox() {
        outboxEventJpaRepository.deleteAll();
    }

    @Test
    void requestDeletion_withinCommittedTransaction_persistsCommittedCleanupOutboxRow() {
        Long materialId = 91L;
        String bucket = "toefl";
        String storageKey = "speaking/91/part1/audio/question_1/old-key.mp3";

        transactionTemplate.executeWithoutResult(status -> {
            storageCleanupAdapter.requestDeletion(materialId, bucket, storageKey);

            verify(storageRepositoryPort, never()).deleteObject(bucket, storageKey);
        });

        List<OutboxEventJpaEntity> events = outboxEventJpaRepository.findAllByOrderByOccurredAtAsc();

        assertThat(events).singleElement().satisfies(event -> {
            assertThat(event.getId()).isNotNull();
            assertThat(event.getAggregateType()).isEqualTo("Material");
            assertThat(event.getAggregateId()).isEqualTo(materialId.toString());
            assertThat(event.getEventType()).isEqualTo(STORAGE_OBJECT_DELETE_REQUESTED);
            assertThat(event.getOccurredAt()).isNotNull();

            JsonNode payload = readPayload(event);
            assertThat(payload.path("materialId").asLong()).isEqualTo(materialId);
            assertThat(payload.path("bucket").asText()).isEqualTo(bucket);
            assertThat(payload.path("storageKey").asText()).isEqualTo(storageKey);
            assertThat(payload.path("requestedAt").asText()).isNotBlank();
            assertThat(Instant.parse(payload.path("requestedAt").asText())).isNotNull();
        });

        verify(storageRepositoryPort, never()).deleteObject(bucket, storageKey);
    }

    @Test
    void requestDeletion_withinRolledBackTransaction_rollsBackCleanupIntent() {
        Long materialId = 92L;
        String bucket = "toefl";
        String storageKey = "speaking/92/part1/image/old-image.png";

        transactionTemplate.executeWithoutResult(status -> {
            storageCleanupAdapter.requestDeletion(materialId, bucket, storageKey);
            status.setRollbackOnly();
        });

        assertThat(outboxEventJpaRepository.findAllByOrderByOccurredAtAsc()).isEmpty();
        verify(storageRepositoryPort, never()).deleteObject(bucket, storageKey);
    }

    @Test
    void requestDeletion_withoutActiveTransaction_failsFastInsteadOfCreatingIndependentOutboxTransaction() {
        Long materialId = 93L;
        String bucket = "toefl";
        String storageKey = "speaking/93/part2/audio/question_3/old-key.mp3";

        assertThatThrownBy(() -> storageCleanupAdapter.requestDeletion(materialId, bucket, storageKey))
                .isInstanceOf(IllegalStateException.class);

        assertThat(outboxEventJpaRepository.findAllByOrderByOccurredAtAsc()).isEmpty();
        verify(storageRepositoryPort, never()).deleteObject(bucket, storageKey);
    }

    private JsonNode readPayload(OutboxEventJpaEntity event) {
        try {
            return objectMapper.readTree(event.getPayload());
        } catch (Exception e) {
            throw new AssertionError("Expected valid JSON payload", e);
        }
    }

    @TestConfiguration
    static class Config {

        @Bean
        @Primary
        StorageRepositoryPort storageRepositoryPort() {
            return mock(StorageRepositoryPort.class);
        }
    }
}

