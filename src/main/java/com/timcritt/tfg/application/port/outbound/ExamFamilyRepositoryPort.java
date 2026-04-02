package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.ExamFamily;

import java.util.Optional;

public interface ExamFamilyRepositoryPort {

    void save(ExamFamily examFamily);
    Optional<ExamFamily> findById(Long id);
    Boolean delete(Long id);

}
