package com.timcritt.tfg.domain.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MaterialDeletedEvent {
    private Long materialId;
    private Long rootNodeId;
    private Instant deletedAt;
}

