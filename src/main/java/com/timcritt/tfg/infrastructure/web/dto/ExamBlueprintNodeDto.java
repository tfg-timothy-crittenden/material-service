package com.timcritt.tfg.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamBlueprintNodeDto {
    private Long id;
    private Long blueprintId;
    private Long parentNodeId;
    private String kind;
    private String code;
    private String title;
    private Integer displayOrder;
    private Long skillId;
    private Long taskTypeId;
    private Boolean isRequired;
    private Integer minChildren;
    private Integer maxChildren;
    private Integer defaultTimeLimitSeconds;
    private Integer defaultPrepTimeSeconds;
    private Map<String, Object> config;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}

