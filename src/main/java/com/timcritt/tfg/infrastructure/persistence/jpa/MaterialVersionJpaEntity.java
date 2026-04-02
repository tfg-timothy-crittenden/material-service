package com.timcritt.tfg.infrastructure.persistence.jpa;

import lombok.*;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "material_version")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialVersionJpaEntity {
    @Id
    private Long id;

    @Column(name = "material_id", nullable = false)
    private Long materialId;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @Column(name = "change_summary")
    private String changeSummary;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "published_at")
    private Instant publishedAt;

    @Column(name = "blueprint_snapshot", columnDefinition = "jsonb")
    @Convert(converter = com.timcritt.tfg.infrastructure.persistence.jpa.MapToJsonConverter.class)
    private Map<String, Object> blueprintSnapshot;

    @Column(name = "is_locked", nullable = false)
    private Boolean isLocked;

    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}

