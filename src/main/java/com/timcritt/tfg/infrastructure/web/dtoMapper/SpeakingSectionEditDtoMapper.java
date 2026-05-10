package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.application.dto.SpeakingQuestionEditResult;
import com.timcritt.tfg.application.dto.SpeakingSectionEditResult;
import com.timcritt.tfg.infrastructure.web.dto.SpeakingSectionEditDto;

public final class SpeakingSectionEditDtoMapper {
    private SpeakingSectionEditDtoMapper() {}

    public static SpeakingSectionEditDto toDto(SpeakingSectionEditResult source) {
        if (source == null) {
            return null;
        }
        return SpeakingSectionEditDto.builder()
                .materialId(source.getMaterialId())
                .sectionId(source.getSectionId())
                .status(source.getStatus())
                .materialTitle(source.getMaterialTitle())
                .materialDescription(source.getMaterialDescription())
                .partTitle(source.getPartTitle())
                .partImageStorageKey(source.getPartImageStorageKey())
                .questions(source.getQuestions() == null ? null : source.getQuestions().stream().map(SpeakingSectionEditDtoMapper::toQuestionDto).toList())
                .part2Title(source.getPart2Title())
                .part2Questions(source.getPart2Questions() == null ? null : source.getPart2Questions().stream().map(SpeakingSectionEditDtoMapper::toQuestionDto).toList())
                .build();
    }

    private static SpeakingSectionEditDto.QuestionEditDto toQuestionDto(SpeakingQuestionEditResult source) {
        if (source == null) {
            return null;
        }
        return SpeakingSectionEditDto.QuestionEditDto.builder()
                .index(source.getIndex())
                .questionNodeId(source.getQuestionNodeId())
                .transcriptText(source.getTranscriptText())
                .config(source.getConfig())
                .audioStorageKey(source.getAudioStorageKey())
                .build();
    }
}

