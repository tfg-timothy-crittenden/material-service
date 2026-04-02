package com.timcritt.tfg.application.service;

import com.timcritt.tfg.application.port.inbound.ExamBlueprintNodeUseCase;
import com.timcritt.tfg.application.port.outbound.ExamBlueprintNodeRepositoryPort;
import com.timcritt.tfg.domain.model.ExamBlueprintNode;
import java.util.List;
import java.util.Optional;

public class ExamBlueprintNodeUseCaseService implements ExamBlueprintNodeUseCase {
    private final ExamBlueprintNodeRepositoryPort repository;

    public ExamBlueprintNodeUseCaseService(ExamBlueprintNodeRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ExamBlueprintNode createExamBlueprintNode(ExamBlueprintNode node) {
        return repository.save(node);
    }

    @Override
    public Optional<ExamBlueprintNode> findExamBlueprintNodeById(Long id) {
        return repository.findById(id);
    }

    @Override
    public List<ExamBlueprintNode> findAllExamBlueprintNodes() {
        return repository.findAll();
    }

    @Override
    public ExamBlueprintNode updateExamBlueprintNode(ExamBlueprintNode node) {
        return repository.save(node);
    }

    @Override
    public void deleteExamBlueprintNode(Long id) {
        repository.deleteById(id);
    }
}

