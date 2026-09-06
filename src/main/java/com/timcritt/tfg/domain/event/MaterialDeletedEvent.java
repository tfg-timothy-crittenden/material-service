package com.timcritt.tfg.domain.event;

import java.time.Instant;

public class MaterialDeletedEvent {
    private Long materialId;
    private Long rootNodeId;
    private Instant deletedAt;

    public MaterialDeletedEvent() {
    }

    public MaterialDeletedEvent(Long materialId, Long rootNodeId, Instant deletedAt) {
        this.materialId = materialId;
        this.rootNodeId = rootNodeId;
        this.deletedAt = deletedAt;
    }

    public static Builder builder() { return new Builder(); }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getRootNodeId() { return rootNodeId; }
    public void setRootNodeId(Long rootNodeId) { this.rootNodeId = rootNodeId; }
    public Instant getDeletedAt() { return deletedAt; }
    public void setDeletedAt(Instant deletedAt) { this.deletedAt = deletedAt; }

    public static class Builder {
        private Long materialId;
        private Long rootNodeId;
        private Instant deletedAt;

        public Builder materialId(Long materialId) { this.materialId = materialId; return this; }
        public Builder rootNodeId(Long rootNodeId) { this.rootNodeId = rootNodeId; return this; }
        public Builder deletedAt(Instant deletedAt) { this.deletedAt = deletedAt; return this; }

        public MaterialDeletedEvent build() {
            return new MaterialDeletedEvent(materialId, rootNodeId, deletedAt);
        }
    }
}

