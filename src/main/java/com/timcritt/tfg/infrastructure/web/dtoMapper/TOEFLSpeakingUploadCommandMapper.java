package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.SpeakingQuestionPartialUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;
import com.timcritt.tfg.application.dto.toefl.UploadedFileCommand;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingSectionUploadDto;
import com.timcritt.tfg.infrastructure.web.dto.TOEFLSpeakingSectionUpdateDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class TOEFLSpeakingUploadCommandMapper {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TOEFLSpeakingUploadCommandMapper() {
    }

    public static TOEFLSpeakingSectionUploadCommand toSectionCommand(TOEFLSpeakingSectionUploadDto dto) {
        if (dto == null) {
            return null;
        }
        return TOEFLSpeakingSectionUploadCommand.builder()
                .materialTitle(dto.getMaterialTitle())
                .materialDescription(dto.getMaterialDescription())
                .materialId(dto.getMaterialId())
                .partTitle(dto.getPartTitle())
                .partImage(toUploadedFile(dto.getPartImage(), "partImage"))
                .questions(toSectionQuestions(dto.getQuestions(), "questions"))
                .part2Title(dto.getPart2Title())
                .part2Questions(toSectionQuestions(dto.getPart2Questions(), "part2Questions"))
                .build();
    }


    private static List<SpeakingQuestionUploadCommand> toSectionQuestions(
            List<TOEFLSpeakingSectionUploadDto.QuestionUpload> questions,
            String fieldName) {
        if (questions == null) {
            return null;
        }
        return questions.stream()
                .map(q -> SpeakingQuestionUploadCommand.builder()
                        .transcriptText(q.getTranscriptText())
                        .config(parseConfig(q.getConfig(), fieldName))
                        .audio(toUploadedFile(q.getAudio(), fieldName + ".audio"))
                        .build())
                .toList();
    }

    private static UploadedFileCommand toUploadedFile(MultipartFile file, String fieldName) {
        if (file == null) {
            return null;
        }
        try {
            return UploadedFileCommand.builder()
                    .originalFilename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .size(file.getSize())
                    .bytes(file.getBytes())
                    .build();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read multipart field '" + fieldName + "'", e);
        }
    }

    private static Map<String, Object> parseConfig(String rawConfig, String fieldName) {
        if (rawConfig == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(rawConfig, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid JSON for field '" + fieldName + ".config'", e);
        }
    }

    public static TOEFLSpeakingSectionUpdateCommand toSectionUpdateCommand(
            Long materialId, TOEFLSpeakingSectionUpdateDto dto) {
        TOEFLSpeakingSectionUpdateCommand.TOEFLSpeakingSectionUpdateCommandBuilder builder =
                TOEFLSpeakingSectionUpdateCommand.builder().materialId(materialId);
        if (dto == null) {
            return builder.build();
        }
        return builder
                .materialTitle(dto.getMaterialTitle())
                .materialDescription(dto.getMaterialDescription())
                .partTitle(dto.getPartTitle())
                .partImage(toUploadedFile(dto.getPartImage(), "partImage"))
                .removePartImage(Boolean.TRUE.equals(dto.getRemovePartImage()))
                .questions(toPartialUpdateCommands(dto.getQuestions(), "questions"))
                .part2Title(dto.getPart2Title())
                .part2Questions(toPartialUpdateCommands(dto.getPart2Questions(), "part2Questions"))
                .build();
    }

    private static List<SpeakingQuestionPartialUpdateCommand> toPartialUpdateCommands(
            List<TOEFLSpeakingSectionUpdateDto.QuestionPartialUpdate> list, String fieldName) {
        if (list == null) {
            return null;
        }
        List<SpeakingQuestionPartialUpdateCommand> commands = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            TOEFLSpeakingSectionUpdateDto.QuestionPartialUpdate q = list.get(i);
            if (q == null) continue;
            boolean hasAudio = q.getAudio() != null && !q.getAudio().isEmpty();
            boolean hasText = q.getTranscriptText() != null && !q.getTranscriptText().isBlank();
            boolean hasConfig = q.getConfig() != null;
            boolean removeAudio = Boolean.TRUE.equals(q.getRemoveAudio());
            if (!hasAudio && !hasText && !hasConfig && !removeAudio) continue; // sparse list: skip empty slots
            commands.add(SpeakingQuestionPartialUpdateCommand.builder()
                    .index(i)
                    .transcriptText(q.getTranscriptText())
                    .config(hasConfig ? parseConfig(q.getConfig(), fieldName + "[" + i + "]") : null)
                    .audio(toUploadedFile(q.getAudio(), fieldName + "[" + i + "].audio"))
                    .removeAudio(removeAudio)
                    .build());
        }
        return commands;
    }
}

