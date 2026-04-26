package com.timcritt.tfg.application.dto.toefl;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class SpeakingQuestionUploadCommand {
    private String transcriptText;
    private Map<String, Object> config;
    private UploadedFileCommand audio;
}

