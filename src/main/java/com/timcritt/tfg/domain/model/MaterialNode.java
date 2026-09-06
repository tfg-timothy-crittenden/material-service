package com.timcritt.tfg.domain.model;

import java.time.Instant;
import java.util.*;

//Represents a section, subsection, part or question of an exam

public class MaterialNode {
    private Long id;
    private Long materialId;
    private Long parentNodeId;
    private String kind;
    private String title;
    private Integer displayOrder;
    private Long skillId;
    private Long taskTypeId;
    private String instructions;
    private String stimulusText;
    private String transcriptText;
    private String explanationText;
    private Integer timeLimitSeconds;
    private Integer prepTimeSeconds;
    private String responseMode;
    private Boolean responseRequired;
    private Integer minDurationSeconds;
    private Integer maxDurationSeconds;
    private Integer minWordCount;
    private Integer maxWordCount;
    private String scoringMode;
    private Double maxScore;
    private Double passingScore;
    private Map<String, Object> config;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    private List<MaterialNode> children = new ArrayList<>();
    private List<MaterialAsset> assets = new ArrayList<>();

    public List<MaterialNode> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public List<MaterialAsset> getAssets() {
        return Collections.unmodifiableList(assets);
    }

    public void addChild(MaterialNode child) {
        Objects.requireNonNull(child, "child cannot be null");

        if (!Objects.equals(materialId, child.materialId)) {
            throw new IllegalArgumentException(
                    "Child node must belong to the same material"
            );
        }

        if (!Objects.equals(id, child.parentNodeId)) {
            throw new IllegalArgumentException(
                    "Child parentNodeId does not match this node"
            );
        }

        children.add(child);
    }

    public void addAsset(MaterialAsset asset) {
        Objects.requireNonNull(asset, "asset cannot be null");

        if (!Objects.equals(id, asset.getMaterialNodeId())) {
            throw new IllegalArgumentException(
                    "Asset does not belong to this material node"
            );
        }

        assets.add(asset);
    }

    public MaterialNode(
            Long id,
            Long materialId,
            Long parentNodeId,
            String kind,
            String title,
            Integer displayOrder,
            Long skillId,
            Long taskTypeId,
            String instructions,
            String stimulusText,
            String transcriptText,
            String explanationText,
            Integer timeLimitSeconds,
            Integer prepTimeSeconds,
            String responseMode,
            Boolean responseRequired,
            Integer minDurationSeconds,
            Integer maxDurationSeconds,
            Integer minWordCount,
            Integer maxWordCount,
            String scoringMode,
            Double maxScore,
            Double passingScore,
            Map<String, Object> config,
            Long version,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.materialId = materialId;
        this.parentNodeId = parentNodeId;
        this.kind = kind;
        this.title = title;
        this.displayOrder = displayOrder;
        this.skillId = skillId;
        this.taskTypeId = taskTypeId;
        this.instructions = instructions;
        this.stimulusText = stimulusText;
        this.transcriptText = transcriptText;
        this.explanationText = explanationText;
        this.timeLimitSeconds = timeLimitSeconds;
        this.prepTimeSeconds = prepTimeSeconds;
        this.responseMode = responseMode;
        this.responseRequired = responseRequired;
        this.minDurationSeconds = minDurationSeconds;
        this.maxDurationSeconds = maxDurationSeconds;
        this.minWordCount = minWordCount;
        this.maxWordCount = maxWordCount;
        this.scoringMode = scoringMode;
        this.maxScore = maxScore;
        this.passingScore = passingScore;
        this.config = config;
        this.version = version;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMaterialId() { return materialId; }
    public void setMaterialId(Long materialId) { this.materialId = materialId; }
    public Long getParentNodeId() { return parentNodeId; }
    public void setParentNodeId(Long parentNodeId) { this.parentNodeId = parentNodeId; }
    public String getKind() { return kind; }
    public void setKind(String kind) { this.kind = kind; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Integer getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(Integer displayOrder) { this.displayOrder = displayOrder; }
    public Long getSkillId() { return skillId; }
    public void setSkillId(Long skillId) { this.skillId = skillId; }
    public Long getTaskTypeId() { return taskTypeId; }
    public void setTaskTypeId(Long taskTypeId) { this.taskTypeId = taskTypeId; }
    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }
    public String getStimulusText() { return stimulusText; }
    public void setStimulusText(String stimulusText) { this.stimulusText = stimulusText; }
    public String getTranscriptText() { return transcriptText; }
    public void setTranscriptText(String transcriptText) { this.transcriptText = transcriptText; }
    public String getExplanationText() { return explanationText; }
    public void setExplanationText(String explanationText) { this.explanationText = explanationText; }
    public Integer getTimeLimitSeconds() { return timeLimitSeconds; }
    public void setTimeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; }
    public Integer getPrepTimeSeconds() { return prepTimeSeconds; }
    public void setPrepTimeSeconds(Integer prepTimeSeconds) { this.prepTimeSeconds = prepTimeSeconds; }
    public String getResponseMode() { return responseMode; }
    public void setResponseMode(String responseMode) { this.responseMode = responseMode; }
    public Boolean getResponseRequired() { return responseRequired; }
    public void setResponseRequired(Boolean responseRequired) { this.responseRequired = responseRequired; }
    public Integer getMinDurationSeconds() { return minDurationSeconds; }
    public void setMinDurationSeconds(Integer minDurationSeconds) { this.minDurationSeconds = minDurationSeconds; }
    public Integer getMaxDurationSeconds() { return maxDurationSeconds; }
    public void setMaxDurationSeconds(Integer maxDurationSeconds) { this.maxDurationSeconds = maxDurationSeconds; }
    public Integer getMinWordCount() { return minWordCount; }
    public void setMinWordCount(Integer minWordCount) { this.minWordCount = minWordCount; }
    public Integer getMaxWordCount() { return maxWordCount; }
    public void setMaxWordCount(Integer maxWordCount) { this.maxWordCount = maxWordCount; }
    public String getScoringMode() { return scoringMode; }
    public void setScoringMode(String scoringMode) { this.scoringMode = scoringMode; }
    public Double getMaxScore() { return maxScore; }
    public void setMaxScore(Double maxScore) { this.maxScore = maxScore; }
    public Double getPassingScore() { return passingScore; }
    public void setPassingScore(Double passingScore) { this.passingScore = passingScore; }
    public Map<String, Object> getConfig() { return config; }
    public void setConfig(Map<String, Object> config) { this.config = config; }
    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public static class Builder {
        private Long id;
        private Long materialId;
        private Long parentNodeId;
        private String kind;
        private String title;
        private Integer displayOrder;
        private Long skillId;
        private Long taskTypeId;
        private String instructions;
        private String stimulusText;
        private String transcriptText;
        private String explanationText;
        private Integer timeLimitSeconds;
        private Integer prepTimeSeconds;
        private String responseMode;
        private Boolean responseRequired;
        private Integer minDurationSeconds;
        private Integer maxDurationSeconds;
        private Integer minWordCount;
        private Integer maxWordCount;
        private String scoringMode;
        private Double maxScore;
        private Double passingScore;
        private Map<String, Object> config;
        private Long version;
        private Instant createdAt;
        private Instant updatedAt;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder materialId(Long materialId) { this.materialId = materialId; return this; }
        public Builder parentNodeId(Long parentNodeId) { this.parentNodeId = parentNodeId; return this; }
        public Builder kind(String kind) { this.kind = kind; return this; }
        public Builder title(String title) { this.title = title; return this; }
        public Builder displayOrder(Integer displayOrder) { this.displayOrder = displayOrder; return this; }
        public Builder skillId(Long skillId) { this.skillId = skillId; return this; }
        public Builder taskTypeId(Long taskTypeId) { this.taskTypeId = taskTypeId; return this; }
        public Builder instructions(String instructions) { this.instructions = instructions; return this; }
        public Builder stimulusText(String stimulusText) { this.stimulusText = stimulusText; return this; }
        public Builder transcriptText(String transcriptText) { this.transcriptText = transcriptText; return this; }
        public Builder explanationText(String explanationText) { this.explanationText = explanationText; return this; }
        public Builder timeLimitSeconds(Integer timeLimitSeconds) { this.timeLimitSeconds = timeLimitSeconds; return this; }
        public Builder prepTimeSeconds(Integer prepTimeSeconds) { this.prepTimeSeconds = prepTimeSeconds; return this; }
        public Builder responseMode(String responseMode) { this.responseMode = responseMode; return this; }
        public Builder responseRequired(Boolean responseRequired) { this.responseRequired = responseRequired; return this; }
        public Builder minDurationSeconds(Integer minDurationSeconds) { this.minDurationSeconds = minDurationSeconds; return this; }
        public Builder maxDurationSeconds(Integer maxDurationSeconds) { this.maxDurationSeconds = maxDurationSeconds; return this; }
        public Builder minWordCount(Integer minWordCount) { this.minWordCount = minWordCount; return this; }
        public Builder maxWordCount(Integer maxWordCount) { this.maxWordCount = maxWordCount; return this; }
        public Builder scoringMode(String scoringMode) { this.scoringMode = scoringMode; return this; }
        public Builder maxScore(Double maxScore) { this.maxScore = maxScore; return this; }
        public Builder passingScore(Double passingScore) { this.passingScore = passingScore; return this; }
        public Builder config(Map<String, Object> config) { this.config = config; return this; }
        public Builder version(Long version) { this.version = version; return this; }
        public Builder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public Builder updatedAt(Instant updatedAt) { this.updatedAt = updatedAt; return this; }

        public MaterialNode build() {
            return new MaterialNode(
                    id,
                    materialId,
                    parentNodeId,
                    kind,
                    title,
                    displayOrder,
                    skillId,
                    taskTypeId,
                    instructions,
                    stimulusText,
                    transcriptText,
                    explanationText,
                    timeLimitSeconds,
                    prepTimeSeconds,
                    responseMode,
                    responseRequired,
                    minDurationSeconds,
                    maxDurationSeconds,
                    minWordCount,
                    maxWordCount,
                    scoringMode,
                    maxScore,
                    passingScore,
                    config,
                    version,
                    createdAt,
                    updatedAt);
        }
    }
}
