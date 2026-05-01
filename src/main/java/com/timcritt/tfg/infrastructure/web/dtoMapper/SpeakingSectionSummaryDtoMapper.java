package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.application.dto.SpeakingSectionSummary;
import com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionSummaryDto;

public final class SpeakingSectionSummaryDtoMapper {
    private SpeakingSectionSummaryDtoMapper() {
    }

    public static SpeakingSectionSummaryDto toDto(SpeakingSectionSummary summary) {
        if (summary == null) {
            return null;
        }
        return SpeakingSectionSummaryDto.builder()
                .materialId(summary.getMaterialId())
                .sectionId(summary.getSectionId())
                .sectionTitle(summary.getSectionTitle())
                .part1Id(summary.getPart1Id())
                .part1Title(summary.getPart1Title())
                .part2Id(summary.getPart2Id())
                .part2Title(summary.getPart2Title())
                .status(summary.getStatus())
                .createdAt(summary.getCreatedAt())
                .updatedAt(summary.getUpdatedAt())
                .build();
    }
}
