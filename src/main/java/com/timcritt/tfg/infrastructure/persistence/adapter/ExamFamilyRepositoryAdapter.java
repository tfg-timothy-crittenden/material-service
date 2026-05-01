package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.application.port.outbound.ExamFamilyRepositoryPort;
import com.timcritt.tfg.domain.model.ExamFamily;
import com.timcritt.tfg.infrastructure.persistence.mapper.ExamFamilyEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.spring.ExamFamilyJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ExamFamilyRepositoryAdapter implements ExamFamilyRepositoryPort {

    private final ExamFamilyJpaRepository repository;

    public ExamFamilyRepositoryAdapter(ExamFamilyJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ExamFamily> findById(Long id) {
        return repository.findById(id).map(ExamFamilyEntityMapper::toDomain);
    }


}
