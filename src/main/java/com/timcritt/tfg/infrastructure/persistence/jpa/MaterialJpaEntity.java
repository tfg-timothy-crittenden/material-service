package com.timcritt.tfg.infrastructure.persistence.jpa;
import com.timcritt.tfg.domain.model.MaterialStatus;
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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "material_id_seq")
    @SequenceGenerator(name = "material_id_seq", sequenceName = "material_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "exam_family_id", nullable = false)
    private Long examFamilyId;

    @Column(name = "material_node_id")
    private Long materialNodeId;

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

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private MaterialStatus status;
}
