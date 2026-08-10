package com.timcritt.tfg.infrastructure.web.dto;

import com.timcritt.tfg.domain.model.MaterialStatus;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(requiredProperties = {
        "materialId", "sectionId", "status", "materialTitle", "materialDescription",
        "partTitle", "partImageStorageKey", "questions", "part2Title", "part2Questions"
})
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
    @Schema(requiredProperties = {"index", "questionNodeId", "transcriptText", "config", "audioStorageKey"})
    public static class QuestionEditDto {
        private Integer index;
        private Long questionNodeId;
        private String transcriptText;

        @Schema(
                description = "Parsed JSON object returned by the backend for question configuration.",
                additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
                example = "{\"prepTimeSeconds\":15,\"timeLimitSeconds\":45}"
        )
        private Map<String, Object> config;

        private String audioStorageKey;
    }
}

