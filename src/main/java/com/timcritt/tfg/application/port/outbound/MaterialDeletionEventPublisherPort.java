package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.application.dto.toefl.MaterialDeletedEvent;

public interface MaterialDeletionEventPublisherPort {
    void publishMaterialDeleted(MaterialDeletedEvent event);
}

