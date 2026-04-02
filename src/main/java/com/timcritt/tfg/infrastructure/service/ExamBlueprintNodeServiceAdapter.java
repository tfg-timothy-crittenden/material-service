package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.inbound.ExamBlueprintNodeUseCase;
import com.timcritt.tfg.application.port.outbound.ExamBlueprintNodeRepositoryPort;
import com.timcritt.tfg.application.service.ExamBlueprintNodeUseCaseService;
import com.timcritt.tfg.domain.model.ExamBlueprintNode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class ExamBlueprintNodeServiceAdapter implements ExamBlueprintNodeUseCase {
    private final ExamBlueprintNodeUseCaseService delegate;

    public ExamBlueprintNodeServiceAdapter(ExamBlueprintNodeRepositoryPort repository) {
        this.delegate = new ExamBlueprintNodeUseCaseService(repository);
    }

    @Override
    @Transactional
    public ExamBlueprintNode createExamBlueprintNode(ExamBlueprintNode node) {
        return delegate.createExamBlueprintNode(node);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ExamBlueprintNode> findExamBlueprintNodeById(Long id) {
        return delegate.findExamBlueprintNodeById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamBlueprintNode> findAllExamBlueprintNodes() {
        return delegate.findAllExamBlueprintNodes();
    }

    @Override
    @Transactional
    public ExamBlueprintNode updateExamBlueprintNode(ExamBlueprintNode node) {
        return delegate.updateExamBlueprintNode(node);
    }

    @Override
    @Transactional
    public void deleteExamBlueprintNode(Long id) {
        delegate.deleteExamBlueprintNode(id);
    }
}

