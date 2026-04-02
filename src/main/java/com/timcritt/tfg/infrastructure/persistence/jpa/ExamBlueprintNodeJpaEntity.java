package com.timcritt.tfg.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "exam_blueprint_node",
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_exam_blueprint_node_code", columnNames = {"blueprint_id", "code"}),
        @UniqueConstraint(name = "uq_exam_blueprint_node_sibling_order", columnNames = {"blueprint_id", "parent_node_id", "display_order"})
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamBlueprintNodeJpaEntity {
    @Id
    private Long id;

    @Column(name = "blueprint_id", nullable = false)
    private Long blueprintId;

    @Column(name = "parent_node_id")
    private Long parentNodeId;

    @Column(nullable = false, length = 30)
    private String kind;

    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 250)
    private String title;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "task_type_id")
    private Long taskTypeId;

    @Column(name = "is_required", nullable = false)
    private Boolean isRequired = true;

    @Column(name = "min_children")
    private Integer minChildren;

    @Column(name = "max_children")
    private Integer maxChildren;

    @Column(name = "default_time_limit_seconds")
    private Integer defaultTimeLimitSeconds;

    @Column(name = "default_prep_time_seconds")
    private Integer defaultPrepTimeSeconds;

    @Column(columnDefinition = "jsonb", nullable = false)
    @Convert(converter = MapToJsonConverter.class)
    private Map<String, Object> config;

    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
