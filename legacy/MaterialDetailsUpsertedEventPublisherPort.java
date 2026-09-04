package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;

/**
 * @deprecated Legacy direct publisher port kept only in archived sources.
 */
@Deprecated
public interface MaterialDetailsUpsertedEventPublisherPort {
    void publishMaterialDetailsUpserted(MaterialDetailsUpsertedEvent event, String requestId);
}


