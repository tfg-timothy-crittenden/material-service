package com.timcritt.tfg.infrastructure.web.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Multipart form binding for a partial update of a TOEFL Speaking section.
 * All fields are optional – omitted fields are not changed.
 */
@Data
public class TOEFLSpeakingSectionUpdateDto {

    private String materialTitle;
    private String materialDescription;

    private String partTitle;
    private MultipartFile partImage;
    private Boolean removePartImage;

    /**
     * Sparse list of Part 1 question updates.
     * Use indexed form params, e.g. {@code questions[2].transcriptText}.
     * Only entries that contain at least one non-empty field are processed.
     */
    private List<QuestionPartialUpdate> questions;

    private String part2Title;

    /**
     * Sparse list of Part 2 question updates (same semantics as {@code questions}).
     */
    private List<QuestionPartialUpdate> part2Questions;

    @Data
    public static class QuestionPartialUpdate {
        private String transcriptText;
        /** JSON string representation of the question config object. */
        private String config;
        private MultipartFile audio;
        private Boolean removeAudio;
    }
}

