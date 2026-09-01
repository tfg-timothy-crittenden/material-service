package com.timcritt.tfg.domain.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDetailsUpsertedEvent {
    private Long materialId;
    private Long version;
    private String materialTitle;
    private String part1Title;
    private String part2Title;
    private String description;
    private Instant updatedAt;
}

