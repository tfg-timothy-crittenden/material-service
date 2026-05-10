package com.timcritt.tfg.application.dto;

import com.timcritt.tfg.domain.model.MaterialStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeakingSectionEditResult {
    private Long materialId;
    private Long sectionId;
    private MaterialStatus status;
    private String materialTitle;
    private String materialDescription;

    private String partTitle;
    private String partImageStorageKey;
    private List<SpeakingQuestionEditResult> questions;

    private String part2Title;
    private List<SpeakingQuestionEditResult> part2Questions;
}

