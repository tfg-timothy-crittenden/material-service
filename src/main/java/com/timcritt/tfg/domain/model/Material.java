package com.timcritt.tfg.domain.model;
import lombok.*;
import java.time.Instant;


//Represents a whole material package, such as a whole TOEFL or CAE exam, with all parts

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Material {
    private Long id;
    private Long examFamilyId;
    private Long blueprintId;
    private Long materialNodeId; // Link to root node
    private String code;
    private String title;
    private String description;
    private Long authorId;
    private Long ownerOrgId;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;
}
