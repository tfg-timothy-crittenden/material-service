package com.timcritt.tfg.infrastructure.persistence.jpa;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;

@Entity
@Table(name = "material_node", uniqueConstraints = {
    @UniqueConstraint(name = "uq_material_node_sibling_order", columnNames = {"parent_node_id", "display_order"})
})
@Getter
@Setter
@DynamicInsert
public class MaterialNodeEntity {
    @Id
    private Long id;

    @Column(name = "parent_node_id")
    private Long parentNodeId;

    @Column(name = "blueprint_node_id")
    private Long blueprintNodeId;

    @Column(name = "kind", nullable = false, length = 30)
    private String kind;

    @Column(name = "code", nullable = false, length = 100)
    private String code;

    @Column(name = "title", length = 250)
    private String title;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;

    @Column(name = "skill_id")
    private Long skillId;

    @Column(name = "task_type_id")
    private Long taskTypeId;

    @Column(name = "instructions")
    private String instructions;

    @Column(name = "stimulus_text")
    private String stimulusText;

    @Column(name = "transcript_text")
    private String transcriptText;

    @Column(name = "explanation_text")
    private String explanationText;

    @Column(name = "time_limit_seconds")
    private Integer timeLimitSeconds;

    @Column(name = "prep_time_seconds")
    private Integer prepTimeSeconds;

    @Column(name = "response_mode", nullable = false, length = 30)
    private String responseMode;

    @Column(name = "response_required", nullable = false)
    private Boolean responseRequired = true;

    @Column(name = "min_duration_seconds")
    private Integer minDurationSeconds;

    @Column(name = "max_duration_seconds")
    private Integer maxDurationSeconds;

    @Column(name = "min_word_count")
    private Integer minWordCount;

    @Column(name = "max_word_count")
    private Integer maxWordCount;

    @Column(name = "scoring_mode", nullable = false, length = 30)
    private String scoringMode;

    @Column(name = "max_score", precision = 8, scale = 2)
    private BigDecimal maxScore;

    @Column(name = "passing_score", precision = 8, scale = 2)
    private BigDecimal passingScore;

    @Column(name = "config", columnDefinition = "jsonb", nullable = false)
    private String config = "{}";

    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (config == null) config = "{}";
        if (responseRequired == null) responseRequired = true;
        if (version == null) version = 0L;
        if (createdAt == null) createdAt = OffsetDateTime.now();
        if (updatedAt == null) updatedAt = OffsetDateTime.now();
    }
}
