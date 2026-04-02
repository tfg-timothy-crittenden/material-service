package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.ExamFamily;

// This interface defines the contract for the use case that will be implemented by the application service.

public interface ExamFamilyUseCase {
    ExamFamily createTest(String newName);
    ExamFamily getTestById(Long id);
    ExamFamily updateTest(Long id, String newName);
    Boolean deleteTest(Long id);
}

