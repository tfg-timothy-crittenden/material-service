package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeDto;

public final class MaterialNodeDtoMapper {
    private MaterialNodeDtoMapper() {}

    public static MaterialNodeDto toDto(MaterialNode d) {
        if (d == null) return null;
        return MaterialNodeDto.builder()
                .id(d.getId())
                .materialVersionId(d.getMaterialVersionId())
                .parentNodeId(d.getParentNodeId())
                .blueprintNodeId(d.getBlueprintNodeId())
                .kind(d.getKind())
                .code(d.getCode())
                .title(d.getTitle())
                .displayOrder(d.getDisplayOrder())
                .skillId(d.getSkillId())
                .taskTypeId(d.getTaskTypeId())
                .instructions(d.getInstructions())
                .stimulusText(d.getStimulusText())
                .transcriptText(d.getTranscriptText())
                .explanationText(d.getExplanationText())
                .timeLimitSeconds(d.getTimeLimitSeconds())
                .prepTimeSeconds(d.getPrepTimeSeconds())
                .responseMode(d.getResponseMode())
                .responseRequired(d.getResponseRequired())
                .minDurationSeconds(d.getMinDurationSeconds())
                .maxDurationSeconds(d.getMaxDurationSeconds())
                .minWordCount(d.getMinWordCount())
                .maxWordCount(d.getMaxWordCount())
                .scoringMode(d.getScoringMode())
                .maxScore(d.getMaxScore())
                .passingScore(d.getPassingScore())
                .config(d.getConfig())
                .version(d.getVersion())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }

    public static MaterialNode toDomain(MaterialNodeDto dto) {
        if (dto == null) return null;
        return MaterialNode.builder()
                .id(dto.getId())
                .materialVersionId(dto.getMaterialVersionId())
                .parentNodeId(dto.getParentNodeId())
                .blueprintNodeId(dto.getBlueprintNodeId())
                .kind(dto.getKind())
                .code(dto.getCode())
                .title(dto.getTitle())
                .displayOrder(dto.getDisplayOrder())
                .skillId(dto.getSkillId())
                .taskTypeId(dto.getTaskTypeId())
                .instructions(dto.getInstructions())
                .stimulusText(dto.getStimulusText())
                .transcriptText(dto.getTranscriptText())
                .explanationText(dto.getExplanationText())
                .timeLimitSeconds(dto.getTimeLimitSeconds())
                .prepTimeSeconds(dto.getPrepTimeSeconds())
                .responseMode(dto.getResponseMode())
                .responseRequired(dto.getResponseRequired())
                .minDurationSeconds(dto.getMinDurationSeconds())
                .maxDurationSeconds(dto.getMaxDurationSeconds())
                .minWordCount(dto.getMinWordCount())
                .maxWordCount(dto.getMaxWordCount())
                .scoringMode(dto.getScoringMode())
                .maxScore(dto.getMaxScore())
                .passingScore(dto.getPassingScore())
                .config(dto.getConfig())
                .version(dto.getVersion())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}

