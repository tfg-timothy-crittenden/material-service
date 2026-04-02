package com.timcritt.tfg.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name = "task_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskTypeJpaEntity {
    @Id
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(name = "exam_family_id")
    private Long examFamilyId;

    @Column(name = "skill_id")
    private Long skillId;

    @Column
    private String description;

    @Column(name = "config_schema", columnDefinition = "jsonb", nullable = false)
    private String configSchema;

    @Version
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

