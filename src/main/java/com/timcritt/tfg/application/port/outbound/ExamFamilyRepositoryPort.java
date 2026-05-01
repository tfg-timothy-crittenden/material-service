package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.ExamFamily;

import java.util.Optional;

public interface ExamFamilyRepositoryPort {
    Optional<ExamFamily> findById(Long id);

}
