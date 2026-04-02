package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.inbound.ExamFamilyUseCase;
import com.timcritt.tfg.application.port.outbound.ExamFamilyRepositoryPort;
import com.timcritt.tfg.application.service.ExamFamilyUseCaseService;
import com.timcritt.tfg.domain.model.ExamFamily;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// This class serves as an adapter that connects the application service implementation (TestUseCaseImpl)
// to the Spring framework. It implements the TestUseCase interface and delegates the actual business logic
// to the TestUseCaseImpl class. The @Service annotation indicates that this class is a Spring-managed component,
// and the @Transactional annotations ensure methods run inside a transactional context.

@Service
public class TestServiceAdapter implements ExamFamilyUseCase {

    private final ExamFamilyUseCaseService delegate;

    public TestServiceAdapter(ExamFamilyRepositoryPort repository) {
        this.delegate = new ExamFamilyUseCaseService(repository);
    }

    @Override
    @Transactional
    public ExamFamily createTest(String newName) {
        return delegate.createTest(newName);
    }

    @Override
    @Transactional(readOnly = true)
    public ExamFamily getTestById(Long id) {
        return delegate.getTestById(id);
    }

    @Override
    @Transactional
    public ExamFamily updateTest(Long id, String newName) {
        return delegate.updateTest(id, newName);
    }

    @Override
    @Transactional
    public Boolean deleteTest(Long id) {
        return delegate.deleteTest(id);
    }
}
