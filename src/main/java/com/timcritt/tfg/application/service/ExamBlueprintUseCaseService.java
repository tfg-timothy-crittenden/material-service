package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.inbound.ExamBlueprintUseCase;
import com.timcritt.tfg.application.port.outbound.ExamBlueprintRepositoryPort;
import com.timcritt.tfg.domain.model.ExamBlueprint;
import java.util.List;
import java.util.Optional;

public class ExamBlueprintUseCaseService implements ExamBlueprintUseCase {
    private final ExamBlueprintRepositoryPort repository;

    public ExamBlueprintUseCaseService(ExamBlueprintRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ExamBlueprint createExamBlueprint(ExamBlueprint examBlueprint) {
        return repository.save(examBlueprint);
    }

    @Override
    public Optional<ExamBlueprint> findExamBlueprintById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<ExamBlueprint> findAllExamBlueprints() {
        return repository.findAll();
    }

    @Override
    public ExamBlueprint updateExamBlueprint(ExamBlueprint examBlueprint) {
        return repository.save(examBlueprint);
    }

    @Override
    public void deleteExamBlueprint(Long id) {
        repository.deleteById(id);
    }
}

