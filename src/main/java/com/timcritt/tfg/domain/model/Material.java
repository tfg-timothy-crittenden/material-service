package com.timcritt.tfg.domain.model;
import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Material {
    private Long id;
    private Long examFamilyId;
    private Long blueprintId;
    private String code;
    private String title;
    private String description;
    private Long authorId;
    private Long ownerOrgId;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}

