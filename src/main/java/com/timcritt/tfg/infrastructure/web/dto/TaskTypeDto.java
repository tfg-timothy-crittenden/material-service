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
public class TaskTypeDto {
    private Long id;
    private String name;
    private String description;
    private Long examFamilyId;
    private Long skillId;
    private String configSchema;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
