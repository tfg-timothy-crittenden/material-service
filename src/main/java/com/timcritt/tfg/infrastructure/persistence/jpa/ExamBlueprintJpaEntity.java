package com.timcritt.tfg.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "exam_blueprint",
    uniqueConstraints = @UniqueConstraint(name = "uq_exam_blueprint_family_name_version", columnNames = {"exam_family_id", "name", "version_no"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamBlueprintJpaEntity {
    @Id
    private Long id;

    @Column(name = "exam_family_id", nullable = false)
    private Long examFamilyId;

    @Column(nullable = false, unique = true, length = 100)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

