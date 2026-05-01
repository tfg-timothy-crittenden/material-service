package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUploadCommand;
import com.timcritt.tfg.application.dto.toefl.TOEFLSpeakingSectionUpdateCommand;

public interface TOEFLSpeakingMaterialCommandUseCase {
    Long uploadSpeakingSection(TOEFLSpeakingSectionUploadCommand command);
    void updateSpeakingSection(TOEFLSpeakingSectionUpdateCommand command);
    void publishSpeakingSection(Long materialId);
}

