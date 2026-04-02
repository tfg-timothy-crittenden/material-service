package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.ExamBlueprintNode;
import java.util.List;
import java.util.Optional;

public interface ExamBlueprintNodeUseCase {
    ExamBlueprintNode createExamBlueprintNode(ExamBlueprintNode node);
    Optional<ExamBlueprintNode> findExamBlueprintNodeById(Long id);
    List<ExamBlueprintNode> findAllExamBlueprintNodes();
    ExamBlueprintNode updateExamBlueprintNode(ExamBlueprintNode node);
    void deleteExamBlueprintNode(Long id);
}

