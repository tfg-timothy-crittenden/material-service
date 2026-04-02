package com.timcritt.tfg.infrastructure.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamBlueprintDto {
    private Long id;
    private Long examFamilyId;
    private String code;
    private String name;
    private Integer versionNo;
    private Boolean isActive;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}

