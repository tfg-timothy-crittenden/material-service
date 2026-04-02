package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.application.port.outbound.ExamFamilyRepositoryPort;
import com.timcritt.tfg.domain.model.ExamFamily;
import com.timcritt.tfg.infrastructure.persistence.jpa.ExamFamilyJpaEntity;
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
    public void save(ExamFamily examFamily) {
        ExamFamilyJpaEntity entity = ExamFamilyEntityMapper.toEntity(examFamily);
        repository.save(entity);
    }

    @Override
    public Optional<ExamFamily> findById(Long id) {
        return repository.findById(id).map(ExamFamilyEntityMapper::toDomain);
    }

    @Override
    public Boolean delete(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }

}
