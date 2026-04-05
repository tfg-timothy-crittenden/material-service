package com.timcritt.tfg.infrastructure.persistence.jpa;
import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "material")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialJpaEntity {
    @Id
    private Long id;

    @Column(name = "exam_family_id", nullable = false)
    private Long examFamilyId;

    @Column(name = "blueprint_id")
    private Long blueprintId;

    @Column(name = "code", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "title", nullable = false, length = 250)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "author_id")
    private Long authorId;

    @Column(name = "owner_org_id")
    private Long ownerOrgId;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "material_node_id")
    private Long materialNodeId;
}
