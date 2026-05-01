package com.timcritt.tfg.infrastructure.web.dto;

import com.timcritt.tfg.domain.model.MaterialStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingSectionSummaryDto {
    private Long materialId;
    private Long sectionId;
    private String sectionTitle;
    private Long part1Id;
    private String part1Title;
    private Long part2Id;
    private String part2Title;
    private MaterialStatus status;
    private Instant createdAt;
    private Instant updatedAt;
}
