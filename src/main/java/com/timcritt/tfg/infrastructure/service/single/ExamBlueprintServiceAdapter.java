package com.timcritt.tfg.infrastructure.service.single;

import com.timcritt.tfg.application.port.inbound.ExamBlueprintUseCase;
import com.timcritt.tfg.application.port.outbound.ExamBlueprintRepositoryPort;
import com.timcritt.tfg.application.service.single.ExamBlueprintUseCaseService;
import com.timcritt.tfg.domain.model.ExamBlueprint;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ExamBlueprintServiceAdapter implements ExamBlueprintUseCase {
    private final ExamBlueprintUseCaseService delegate;

    public ExamBlueprintServiceAdapter(ExamBlueprintRepositoryPort repository) {
        this.delegate = new ExamBlueprintUseCaseService(repository);
    }

    @Override
    @Transactional
    public ExamBlueprint createExamBlueprint(ExamBlueprint examBlueprint) {
        return delegate.createExamBlueprint(examBlueprint);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExamBlueprint> findExamBlueprintById(Long id) {
        return delegate.findExamBlueprintById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamBlueprint> findAllExamBlueprints() {
        return delegate.findAllExamBlueprints();
    }

    @Override
    @Transactional
    public ExamBlueprint updateExamBlueprint(ExamBlueprint examBlueprint) {
        return delegate.updateExamBlueprint(examBlueprint);
    }

    @Override
    @Transactional
    public void deleteExamBlueprint(Long id) {
        delegate.deleteExamBlueprint(id);
    }
}

