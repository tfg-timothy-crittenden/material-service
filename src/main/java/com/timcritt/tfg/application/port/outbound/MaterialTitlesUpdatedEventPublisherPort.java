package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.event.MaterialTitlesUpdatedEvent;

public interface MaterialTitlesUpdatedEventPublisherPort {
    void publishMaterialTitlesUpdated(MaterialTitlesUpdatedEvent event);
}

