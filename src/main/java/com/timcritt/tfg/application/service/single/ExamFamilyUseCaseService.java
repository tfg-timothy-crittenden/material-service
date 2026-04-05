package com.timcritt.tfg.application.service.single;

import com.timcritt.tfg.application.port.inbound.ExamFamilyUseCase;
import com.timcritt.tfg.application.port.outbound.ExamFamilyRepositoryPort;
import com.timcritt.tfg.domain.model.ExamFamily;




public class ExamFamilyUseCaseService implements ExamFamilyUseCase {

    private final ExamFamilyRepositoryPort repository;

    public ExamFamilyUseCaseService(ExamFamilyRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ExamFamily createTest(String newName) {
        ExamFamily newItem = new ExamFamily();
        newItem.setName(newName);
        repository.save(newItem);
        return newItem;
    }

    @Override
    public ExamFamily getTestById(Long id) {
        return repository.findById(id).orElseThrow();
    }

    @Override
    public ExamFamily updateTest(Long id, String newName) {
        ExamFamily existing = repository.findById(id).orElseThrow();
        existing.setName(newName);
        repository.save(existing);
        return existing;
    }

    @Override
    public Boolean deleteTest(Long id) {
        ExamFamily existing = repository.findById(id).orElseThrow();
        return repository.delete(existing.getId());
    }
}
