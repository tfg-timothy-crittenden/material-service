package com.timcritt.tfg.application.port.outbound;

import java.util.UUID;

public interface IntegrationEventOutboxPort {

    void append(
            UUID eventId,
            String aggregateType,
            String aggregateId,
            String eventType,
            Object payload
    );
}