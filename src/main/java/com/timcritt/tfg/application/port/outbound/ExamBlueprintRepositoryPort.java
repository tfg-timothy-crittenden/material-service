package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.ExamBlueprint;
import java.util.List;
import java.util.Optional;

public interface ExamBlueprintRepositoryPort {
    ExamBlueprint save(ExamBlueprint examBlueprint);
    Optional<ExamBlueprint> findById(Long id);
    List<ExamBlueprint> findAll();
    void deleteById(Long id);
}

