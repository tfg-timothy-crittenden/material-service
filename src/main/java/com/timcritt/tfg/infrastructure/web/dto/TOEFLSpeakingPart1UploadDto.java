package com.timcritt.tfg.infrastructure.web.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

//This holds only fields that are set in the front. Other columns are set to values associated with TOEFL Part 1 Speaking
@Data
public class TOEFLSpeakingPart1UploadDto {

    @NotBlank
    private String materialTitle;
    private String materialDescription;

    // Optional: if provided, will be ignored in favor of new material creation
    private Long materialId;

    @NotNull
    private MultipartFile partImage;

    @NotEmpty
    @Valid
    private List<@Valid QuestionUpload> questions;

    @NotBlank
    private String partTitle;

    @Data
    public static class QuestionUpload {
        @NotBlank
        private String transcriptText;
        @NotNull
        private MultipartFile audio;

        // Accept as String for multipart/form-data, parse to Map<String, Object> in service
        @NotNull
        @JsonProperty("config")
        private String config;
    }
}
