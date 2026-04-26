package com.timcritt.tfg.application.dto.toefl;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Command for a partial update of a TOEFL Speaking section.
 * Every field is optional – null means "keep the existing value".
 */
@Data
@Builder
public class TOEFLSpeakingSectionUpdateCommand {
    /** ID of the material (section) to update. Required. */
    private Long materialId;

    /** If non-null and non-blank, updates the material title and root-section-node title. */
    private String materialTitle;
    /** If non-null, updates the material description (empty string explicitly clears it). */
    private String materialDescription;

    /** If non-null and non-blank, updates the Part 1 title. */
    private String partTitle;
    /** If non-null, replaces the Part 1 image in storage and in the DB. */
    private UploadedFileCommand partImage;
    /**
     * Partial updates for Part 1 questions.
     * Only entries that contain at least one non-null payload field are applied;
     * each entry's {@code index} field identifies which question (0-based display order)
     * to update. Null list means "do not modify any questions".
     */
    private List<SpeakingQuestionPartialUpdateCommand> questions;

    /** If non-null and non-blank, updates the Part 2 title. */
    private String part2Title;
    /** Partial updates for Part 2 questions (same semantics as {@code questions}). */
    private List<SpeakingQuestionPartialUpdateCommand> part2Questions;
}

