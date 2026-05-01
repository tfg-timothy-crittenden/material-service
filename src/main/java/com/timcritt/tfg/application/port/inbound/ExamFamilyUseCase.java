package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.ExamFamily;

// This interface defines the contract for the use case that will be implemented by the application service.

public interface ExamFamilyUseCase {
    ExamFamily getTestById(Long id);
}

