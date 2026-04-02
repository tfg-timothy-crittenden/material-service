package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.ExamBlueprint;
import java.util.List;
import java.util.Optional;

public interface ExamBlueprintUseCase {
    ExamBlueprint createExamBlueprint(ExamBlueprint examBlueprint);
    Optional<ExamBlueprint> findExamBlueprintById(Long id);
    List<ExamBlueprint> findAllExamBlueprints();
    ExamBlueprint updateExamBlueprint(ExamBlueprint examBlueprint);
    void deleteExamBlueprint(Long id);
}

