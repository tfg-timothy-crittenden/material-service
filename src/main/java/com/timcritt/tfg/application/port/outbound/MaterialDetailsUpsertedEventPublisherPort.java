package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;

public interface MaterialDetailsUpsertedEventPublisherPort {
    void publishMaterialDetailsUpserted(MaterialDetailsUpsertedEvent event, String requestId);
}

