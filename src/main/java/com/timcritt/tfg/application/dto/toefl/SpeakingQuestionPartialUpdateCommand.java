package com.timcritt.tfg.application.dto.toefl;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Represents a partial update for a single speaking question.
 * All fields are optional – only non-null values are applied;
 * null means "keep the existing value".
 */
@Data
@Builder
public class SpeakingQuestionPartialUpdateCommand {
    /** Zero-based position of the question under its parent PART node. */
    private int index;
    /** If non-null and non-blank, replaces the existing transcriptText. */
    private String transcriptText;
    /** If non-null, replaces the existing config. */
    private Map<String, Object> config;
    /** If non-null, the existing audio asset is replaced with this file. */
    private UploadedFileCommand audio;
    /** If true, removes the existing audio asset for this question. */
    private Boolean removeAudio;
}

