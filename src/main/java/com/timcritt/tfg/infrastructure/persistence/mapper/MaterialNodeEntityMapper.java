package com.timcritt.tfg.infrastructure.persistence.mapper;

import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;

public class MaterialNodeEntityMapper {
    public static MaterialNode toDomain(MaterialNodeJpaEntity entity) {
        if (entity == null) return null;
        return MaterialNode.builder()
                .id(entity.getId())
                .materialVersionId(null)
                .parentNodeId(entity.getParentNodeId())
                .kind(entity.getKind())
                .title(entity.getTitle())
                .displayOrder(entity.getDisplayOrder())
                .skillId(entity.getSkillId())
                .taskTypeId(entity.getTaskTypeId())
                .instructions(entity.getInstructions())
                .stimulusText(entity.getStimulusText())
                .transcriptText(entity.getTranscriptText())
                .explanationText(entity.getExplanationText())
                .timeLimitSeconds(entity.getTimeLimitSeconds())
                .prepTimeSeconds(entity.getPrepTimeSeconds())
                .responseMode(entity.getResponseMode())
                .responseRequired(entity.getResponseRequired())
                .minDurationSeconds(entity.getMinDurationSeconds())
                .maxDurationSeconds(entity.getMaxDurationSeconds())
                .minWordCount(entity.getMinWordCount())
                .maxWordCount(entity.getMaxWordCount())
                .scoringMode(entity.getScoringMode())
                .maxScore(entity.getMaxScore())
                .passingScore(entity.getPassingScore())
                .config(entity.getConfig())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static MaterialNodeJpaEntity toEntity(MaterialNode domain) {
        if (domain == null) return null;
        return MaterialNodeJpaEntity.builder()
                .id(domain.getId())
                .parentNodeId(domain.getParentNodeId())
                .kind(domain.getKind())
                .title(domain.getTitle())
                .displayOrder(domain.getDisplayOrder())
                .skillId(domain.getSkillId())
                .taskTypeId(domain.getTaskTypeId())
                .instructions(domain.getInstructions())
                .stimulusText(domain.getStimulusText())
                .transcriptText(domain.getTranscriptText())
                .explanationText(domain.getExplanationText())
                .timeLimitSeconds(domain.getTimeLimitSeconds())
                .prepTimeSeconds(domain.getPrepTimeSeconds())
                .responseMode(domain.getResponseMode())
                .responseRequired(domain.getResponseRequired())
                .minDurationSeconds(domain.getMinDurationSeconds())
                .maxDurationSeconds(domain.getMaxDurationSeconds())
                .minWordCount(domain.getMinWordCount())
                .maxWordCount(domain.getMaxWordCount())
                .scoringMode(domain.getScoringMode())
                .maxScore(domain.getMaxScore())
                .passingScore(domain.getPassingScore())
                .config(domain.getConfig())
                .version(domain.getVersion())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
