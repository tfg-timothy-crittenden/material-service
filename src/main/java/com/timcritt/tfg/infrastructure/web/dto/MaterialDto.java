package com.timcritt.tfg.infrastructure.web.dto;
import lombok.*;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialDto {
    private Long id;
    private Long examFamilyId;
    private String title;
    private String description;
    private Long authorId;
    private Long ownerOrgId;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
    private Long materialNodeId; // Link to root node
}
