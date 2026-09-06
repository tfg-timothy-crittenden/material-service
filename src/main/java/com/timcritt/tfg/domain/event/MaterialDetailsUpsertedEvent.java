package com.timcritt.tfg.domain.event;

import java.time.Instant;

public class MaterialDetailsUpsertedEvent {
    private Long materialId;
    private Long version;
    private String materialTitle;
    private String part1Title;
    private String part2Title;
    private String description;
    private Instant updatedAt;

    public MaterialDetailsUpsertedEvent() {
    }

    public MaterialDetailsUpsertedEvent(Long materialId, Long version, String materialTitle, String part1Title, String part2Title, String description, Instant updatedAt) {
        this.materialId = materialId;
        this.version = version;
        this.materialTitle = materialTitle;
        this.part1Title = part1Title;
        this.part2Title = part2Title;
        this.description = description;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public String getMaterialTitle() { return materialTitle; }
    public void setMaterialTitle(String materialTitle) { this.materialTitle = materialTitle; }
    public String getPart1Title() { return part1Title; }
    public void setPart1Title(String part1Title) { this.part1Title = part1Title; }
    public String getPart2Title() { return part2Title; }
    public void setPart2Title(String part2Title) { this.part2Title = part2Title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private Long materialId;
        private Long version;
        private String materialTitle;
        private String part1Title;
        private String part2Title;
        private String description;
        private Instant updatedAt;

        public Builder materialId(Long materialId) { this.materialId = materialId; return this; }
        public Builder version(Long version) { this.version = version; return this; }
        public Builder materialTitle(String materialTitle) { this.materialTitle = materialTitle; return this; }
        public Builder part1Title(String part1Title) { this.part1Title = part1Title; return this; }
        public Builder part2Title(String part2Title) { this.part2Title = part2Title; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public MaterialDetailsUpsertedEvent build() {
            return new MaterialDetailsUpsertedEvent(materialId, version, materialTitle, part1Title, part2Title, description, updatedAt);
        }
    }
}

