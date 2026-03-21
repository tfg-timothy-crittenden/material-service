package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.exception.TestNotFoundException;
import com.timcritt.tfg.application.port.inbound.TestUseCase;
import com.timcritt.tfg.application.port.outbound.TestRepositoryPort;
import com.timcritt.tfg.domain.model.TestItem;

// This class is the implementation of the TestUseCase interface and contains the business logic for handling TestItem operations.
// It avoids any framework-specific code and focuses solely on the application logic, making it easy to test and maintain.

public class TestUseCaseImpl implements TestUseCase {

    private final TestRepositoryPort repository;

    public TestUseCaseImpl(TestRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public TestItem getTestById(Long id) {
        return repository.findById(id).orElseThrow(() -> new TestNotFoundException(id));
    }

    @Override
    public TestItem updateTest(Long id, String newName) {
        TestItem existing = repository.findById(id).orElseThrow(() -> new TestNotFoundException(id));
        existing.setName(newName);
        return repository.save(existing);
    }

    @Override
    public TestItem createTest(String newName) {
        TestItem newItem = new TestItem();
        newItem.setName(newName);
        return repository.save(newItem);
    }

    @Override
    public Boolean deleteTest(Long id) {
        TestItem existing = repository.findById(id).orElseThrow(() -> new TestNotFoundException(id));
        return repository.delete(existing.getId());

    }
}

