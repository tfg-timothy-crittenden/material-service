package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.ExamBlueprintNode;
import java.util.List;
import java.util.Optional;

public interface ExamBlueprintNodeRepositoryPort {
    ExamBlueprintNode save(ExamBlueprintNode node);
    Optional<ExamBlueprintNode> findById(Long id);
    List<ExamBlueprintNode> findAll();
    void deleteById(Long id);
}

