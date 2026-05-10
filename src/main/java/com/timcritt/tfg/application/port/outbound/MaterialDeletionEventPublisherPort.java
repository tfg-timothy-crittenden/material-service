package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.event.MaterialDeletedEvent;

public interface MaterialDeletionEventPublisherPort {
    void publishMaterialDeleted(MaterialDeletedEvent event);
}

