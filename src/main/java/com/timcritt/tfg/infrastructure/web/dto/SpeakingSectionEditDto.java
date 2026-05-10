package com.timcritt.tfg.infrastructure.web.dto;

import com.timcritt.tfg.domain.model.MaterialStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingSectionEditDto {
    private Long materialId;
    private Long sectionId;
    private MaterialStatus status;
    private String materialTitle;
    private String materialDescription;

    private String partTitle;
    private String partImageStorageKey;
    private List<QuestionEditDto> questions;

    private String part2Title;
    private List<QuestionEditDto> part2Questions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuestionEditDto {
        private Integer index;
        private Long questionNodeId;
        private String transcriptText;
        private Map<String, Object> config;
        private String audioStorageKey;
    }
}

