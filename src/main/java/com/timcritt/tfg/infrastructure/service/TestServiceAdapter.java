package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.outbound.TestRepositoryPort;
import com.timcritt.tfg.application.service.TestUseCaseImpl;
import com.timcritt.tfg.application.port.inbound.TestUseCase;
import com.timcritt.tfg.domain.model.TestItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// This class serves as an adapter that connects the application service implementation (TestUseCaseImpl)
// to the Spring framework. It implements the TestUseCase interface and delegates the actual business logic
// to the TestUseCaseImpl class. The @Service annotation indicates that this class is a Spring-managed component,
// and the @Transactional annotations ensure methods run inside a transactional context.

@Service
public class TestServiceAdapter implements TestUseCase {

    private final TestUseCaseImpl delegate;

    public TestServiceAdapter(TestRepositoryPort repository) {
        this.delegate = new TestUseCaseImpl(repository);
    }

    @Override
    @Transactional
    public TestItem createTest(String newName) {
        return delegate.createTest(newName);
    }

    @Override
    @Transactional(readOnly = true)
    public TestItem getTestById(Long id) {
        return delegate.getTestById(id);
    }

    @Override
    @Transactional
    public TestItem updateTest(Long id, String newName) {
        return delegate.updateTest(id, newName);
    }

    @Override
    @Transactional
    public Boolean deleteTest(Long id) {
        return delegate.deleteTest(id);
    }
}
