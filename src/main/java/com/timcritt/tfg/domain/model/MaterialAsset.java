package com.timcritt.tfg.domain.model;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MaterialAsset {

    private Long id;
    private Long materialNodeId;
    private Kind kind;
    private String storageKey;
    private String originalFilename;
    private String mimeType;
    private Long fileSizeBytes;
    private String title;
    private String transcriptText;
    private Integer displayOrder;
    private Map<String, Object> metadata;
    private Long version;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    public enum Kind {
        TEXT,
        AUDIO,
        IMAGE,
        VIDEO,
        PDF,
        OTHER
    }

    private MaterialAsset(
            Long id,
            Long materialNodeId,
            Kind kind,
            String storageKey,
            String originalFilename,
            String mimeType,
            Long fileSizeBytes,
            String title,
            String transcriptText,
            Integer displayOrder,
            Map<String, Object> metadata,
            Long version,
            OffsetDateTime createdAt,
            OffsetDateTime updatedAt
    ) {
        this.id = id;
        this.materialNodeId = materialNodeId;
        this.kind = kind;
        this.storageKey = storageKey;
        this.originalFilename = originalFilename;
        this.mimeType = mimeType;
        this.fileSizeBytes = fileSizeBytes;
        this.title = title;
        this.transcriptText = transcriptText;
        this.displayOrder = displayOrder;
        this.metadata = metadata == null
                ? new HashMap<>()
                : new HashMap<>(metadata);
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static MaterialAsset create(
            Long materialNodeId,
            Kind kind,
            String storageKey,
            String originalFilename,
            String mimeType,
            Long fileSizeBytes
    ) {
        if (materialNodeId == null) {
            throw new IllegalArgumentException("materialNodeId cannot be null");
        }

        if (kind == null) {
            throw new IllegalArgumentException("kind cannot be null");
        }

        if (storageKey == null || storageKey.isBlank()) {
            throw new IllegalArgumentException("storageKey cannot be blank");
        }

        if (fileSizeBytes != null && fileSizeBytes < 0) {
            throw new IllegalArgumentException("fileSizeBytes cannot be negative");
        }

        OffsetDateTime now = OffsetDateTime.now();

        return new MaterialAsset(
                null,
                materialNodeId,
                kind,
                storageKey.trim(),
                normalize(originalFilename),
                normalize(mimeType),
                fileSizeBytes,
                null,
                null,
                0,
                new HashMap<>(),
                0L,
                now,
                now
        );
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public Long getMaterialNodeId() {
        return materialNodeId;
    }

    public Kind getKind() {
        return kind;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getMimeType() {
        return mimeType;
    }

    public Long getFileSizeBytes() {
        return fileSizeBytes;
    }

    public String getTitle() {
        return title;
    }

    public String getTranscriptText() {
        return transcriptText;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public Map<String, Object> getMetadata() {
        return metadata == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(metadata);
    }

    public Long getVersion() {
        return version;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static class Builder {

        private Long id;
        private Long materialNodeId;
        private Kind kind;
        private String storageKey;
        private String originalFilename;
        private String mimeType;
        private Long fileSizeBytes;
        private String title;
        private String transcriptText;
        private Integer displayOrder;
        private Map<String, Object> metadata;
        private Long version;
        private OffsetDateTime createdAt;
        private OffsetDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder materialNodeId(Long materialNodeId) {
            this.materialNodeId = materialNodeId;
            return this;
        }

        public Builder kind(Kind kind) {
            this.kind = kind;
            return this;
        }

        public Builder storageKey(String storageKey) {
            this.storageKey = storageKey;
            return this;
        }

        public Builder originalFilename(String originalFilename) {
            this.originalFilename = originalFilename;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder fileSizeBytes(Long fileSizeBytes) {
            this.fileSizeBytes = fileSizeBytes;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder transcriptText(String transcriptText) {
            this.transcriptText = transcriptText;
            return this;
        }

        public Builder displayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
            return this;
        }

        public Builder metadata(Map<String, Object> metadata) {
            this.metadata = metadata;
            return this;
        }

        public Builder version(Long version) {
            this.version = version;
            return this;
        }

        public Builder createdAt(OffsetDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(OffsetDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public MaterialAsset build() {
            return new MaterialAsset(
                    id,
                    materialNodeId,
                    kind,
                    storageKey,
                    originalFilename,
                    mimeType,
                    fileSizeBytes,
                    title,
                    transcriptText,
                    displayOrder,
                    metadata,
                    version,
                    createdAt,
                    updatedAt
            );
        }
    }
}