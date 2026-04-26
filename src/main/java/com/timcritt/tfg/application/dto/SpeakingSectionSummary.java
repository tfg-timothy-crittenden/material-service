package com.timcritt.tfg.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingSectionSummary {
    private Long sectionId;
    private String sectionTitle;
    private Long part1Id;
    private String part1Title;
    private Long part2Id;
    private String part2Title;
}

