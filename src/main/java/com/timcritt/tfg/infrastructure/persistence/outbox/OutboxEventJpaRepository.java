package com.timcritt.tfg.infrastructure.persistence.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OutboxEventJpaRepository
        extends JpaRepository<OutboxEventJpaEntity, UUID> {
}