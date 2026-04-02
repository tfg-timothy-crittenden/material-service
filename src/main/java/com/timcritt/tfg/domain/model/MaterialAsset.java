package com.timcritt.tfg.domain.model;

import java.time.OffsetDateTime;
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
        TEXT, AUDIO, IMAGE, VIDEO, PDF, OTHER
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMaterialNodeId() { return materialNodeId; }
    public void setMaterialNodeId(Long materialNodeId) { this.materialNodeId = materialNodeId; }
    public Kind getKind() { return kind; }
    public void setKind(Kind kind) { this.kind = kind; }
    public String getStorageKey() { return storageKey; }
    public void setStorageKey(String storageKey) { this.storageKey = storageKey; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public Long getFileSizeBytes() { return fileSizeBytes; }
    public void setFileSizeBytes(Long fileSizeBytes) { this.fileSizeBytes = fileSizeBytes; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getTranscriptText() { return transcriptText; }
    public void setTranscriptText(String transcriptText) { this.transcriptText = transcriptText; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
