package com.timcritt.tfg.infrastructure.persistence.jpa;

import com.timcritt.tfg.infrastructure.persistence.auxiliary.MapToJsonConverter;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "material_node")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialNodeJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "material_node_id_seq")
    @SequenceGenerator(name = "material_node_id_seq", sequenceName = "material_node_id_seq", allocationSize = 1)
    private Long id;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "parent_node_id")
    private Long parentNodeId;

    @Column(name = "kind", nullable = false)
    private String kind;

    @Column(name = "title")
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

    @Column(name = "response_mode", nullable = false)
    private String responseMode;

    @Column(name = "response_required", nullable = false)
    private Boolean responseRequired;

    @Column(name = "min_duration_seconds")
    private Integer minDurationSeconds;

    @Column(name = "max_duration_seconds")
    private Integer maxDurationSeconds;

    @Column(name = "min_word_count")
    private Integer minWordCount;

    @Column(name = "max_word_count")
    private Integer maxWordCount;

    @Column(name = "scoring_mode", nullable = false)
    private String scoringMode;

    @Column(name = "max_score")
    private Double maxScore;

    @Column(name = "passing_score")
    private Double passingScore;

    @Convert(converter = MapToJsonConverter.class)
    @Column(name = "config", columnDefinition = "jsonb", nullable = false)
    private Map<String, Object> config;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
