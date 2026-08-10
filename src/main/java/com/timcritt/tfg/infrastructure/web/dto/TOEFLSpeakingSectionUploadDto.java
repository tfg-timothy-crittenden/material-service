package com.timcritt.tfg.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class TOEFLSpeakingSectionUploadDto {

    @NotBlank
    private String materialTitle;

    private String materialDescription;

    private Long materialId;

    @NotNull
    private MultipartFile partImage;

    @NotBlank
    private String partTitle;

    @NotEmpty
    @Valid
    private List<@Valid QuestionUpload> questions;

    @NotBlank
    private String part2Title;

    @NotEmpty
    @Size(min = 4, max = 4)
    @Valid
    private List<@Valid QuestionUpload> part2Questions;

    @Data
    public static class QuestionUpload {
        @NotBlank
        private String transcriptText;

        @NotNull
        private MultipartFile audio;

        @Schema(
                description = "Question config as a JSON string in multipart form data. Example: {\"prepTimeSeconds\":15}",
                type = "string"
        )
        private String config;
    }
}

