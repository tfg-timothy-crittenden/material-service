package com.timcritt.tfg.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingQuestionEditResult {
    private Integer index;
    private Long questionNodeId;
    private String transcriptText;
    private Map<String, Object> config;
    private String audioStorageKey;
}

