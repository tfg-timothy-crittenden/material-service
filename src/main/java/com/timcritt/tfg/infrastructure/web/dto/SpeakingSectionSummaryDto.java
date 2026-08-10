package com.timcritt.tfg.infrastructure.web.dto;

import com.timcritt.tfg.domain.model.MaterialStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(requiredProperties = {
        "materialId", "sectionId", "sectionTitle", "part1Id", "part1Title",
        "part2Id", "part2Title", "status", "createdAt", "updatedAt"
})
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
