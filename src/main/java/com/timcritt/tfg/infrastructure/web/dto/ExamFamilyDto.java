package com.timcritt.tfg.infrastructure.web.dto;

import lombok.Data;
import java.time.Instant;

@Data
public class ExamFamilyDto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
