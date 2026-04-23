package com.timcritt.tfg.infrastructure.persistence.jpa;

import com.timcritt.tfg.infrastructure.persistence.auxiliary.JsonbConverter;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Entity
@Table(name = "material_asset")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterialAssetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "material_asset_id_seq")
    @SequenceGenerator(name = "material_asset_id_seq", sequenceName = "material_asset_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "material_node_id", nullable = false)
    private MaterialNodeJpaEntity materialNode;

    @Column(nullable = false, length = 30)
    @Enumerated(EnumType.STRING)
    private Kind kind;

    @Column(name = "storage_key", nullable = false, length = 500)
    private String storageKey;

    @Column(name = "original_filename", length = 255)
    private String originalFilename;

    @Column(name = "mime_type", length = 150)
    private String mimeType;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(length = 250)
    private String title;

    @Column(name = "transcript_text", columnDefinition = "TEXT")
    private String transcriptText;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(columnDefinition = "json", nullable = false)
    @Convert(converter = JsonbConverter.class)
    private Map<String, Object> metadata;

    @Column(nullable = false)
    private Long version = 0L;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public enum Kind {
        TEXT, AUDIO, IMAGE, VIDEO, PDF, OTHER
    }
}
