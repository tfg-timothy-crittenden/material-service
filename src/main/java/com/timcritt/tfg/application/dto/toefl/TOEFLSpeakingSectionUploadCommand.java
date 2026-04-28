package com.timcritt.tfg.application.dto.toefl;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TOEFLSpeakingSectionUploadCommand {
    private String materialTitle;
    private String materialDescription;
    private Long materialId;
    private String partTitle;
    private UploadedFileCommand partImage;
    private List<SpeakingQuestionUploadCommand> questions;
    private String part2Title;
    private List<SpeakingQuestionUploadCommand> part2Questions;
}

