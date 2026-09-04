package com.timcritt.tfg.application.integration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.timcritt.tfg.domain.event.MaterialDetailsUpsertedEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDetailsUpsertedOutboxMessage {
    private String requestId;
    private MaterialDetailsUpsertedEvent event;
}

