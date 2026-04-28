package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingPart1UploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;

public interface TOEFLSpeakingMaterialCommandUseCase {
    void uploadSpeakingPart1(TOEFLSpeakingPart1UploadCommand command);
    void uploadSpeakingSection(TOEFLSpeakingSectionUploadCommand command);
    void updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand command);
}

