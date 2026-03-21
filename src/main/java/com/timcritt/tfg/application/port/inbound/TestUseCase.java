package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.TestItem;

// This interface defines the contract for the use case that will be implemented by the application service.

public interface TestUseCase {
    TestItem createTest(String newName);
    TestItem getTestById(Long id);
    TestItem updateTest(Long id, String newName);
    Boolean deleteTest(Long id);
}

