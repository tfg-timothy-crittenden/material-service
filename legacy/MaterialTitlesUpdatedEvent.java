package com.timcritt.tfg.domain.event;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class MaterialTitlesUpdatedEvent {
    private Long materialId;
    private Long version;
    private String materialTitle;
    private String part1Title;
    private String part2Title;
    private Instant updatedAt;
}


