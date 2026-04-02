package com.timcritt.tfg.infrastructure.web.dto;

import lombok.*;
import java.time.Instant;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialNodeDto {
    private Long id;
    private Long materialVersionId;
    private Long parentNodeId;
    private Long blueprintNodeId;
    private String kind;
    private String code;
    private String title;
    private Integer displayOrder;
    private Long skillId;
    private Long taskTypeId;
    private String instructions;
    private String stimulusText;
    private String transcriptText;
    private String explanationText;
    private Integer timeLimitSeconds;
    private Integer prepTimeSeconds;
    private String responseMode;
    private Boolean responseRequired;
    private Integer minDurationSeconds;
    private Integer maxDurationSeconds;
    private Integer minWordCount;
    private Integer maxWordCount;
    private String scoringMode;
    private Double maxScore;
    private Double passingScore;
    private Map<String, Object> config;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}

