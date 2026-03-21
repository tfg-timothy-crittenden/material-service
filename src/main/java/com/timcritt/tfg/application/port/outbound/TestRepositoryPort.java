package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.TestItem;
import java.util.Optional;


// This interface defines the contract for the repository that will be used by the application service.

public interface TestRepositoryPort {
    Optional<TestItem> findById(Long id);
    TestItem save(TestItem item);
    Boolean delete(Long id);
}

