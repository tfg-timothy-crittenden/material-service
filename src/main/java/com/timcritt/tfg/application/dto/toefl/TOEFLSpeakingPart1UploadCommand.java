package com.timcritt.tfg.application.dto.toefl;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class TOEFLSpeakingPart1UploadCommand {
    private String materialTitle;
    private String materialDescription;
    private Long materialId;
    private String partTitle;
    private UploadedFileCommand partImage;
    private List<SpeakingQuestionUploadCommand> questions;
}

