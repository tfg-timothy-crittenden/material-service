package com.timcritt.tfg.application.service.single;

import com.timcritt.tfg.application.port.inbound.ExamFamilyUseCase;
import com.timcritt.tfg.application.port.outbound.ExamFamilyRepositoryPort;
import com.timcritt.tfg.domain.model.ExamFamily;




public class ExamFamilyUseCaseService implements ExamFamilyUseCase {

    private final ExamFamilyRepositoryPort repository;

    public ExamFamilyUseCaseService(ExamFamilyRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public ExamFamily getTestById(Long id) {
        return repository.findById(id).orElseThrow();
    }
}
