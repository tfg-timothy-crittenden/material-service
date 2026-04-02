package com.timcritt.tfg.infrastructure.persistence;

import com.timcritt.tfg.application.port.outbound.ExamBlueprintRepositoryPort;
import com.timcritt.tfg.domain.model.ExamBlueprint;
import com.timcritt.tfg.infrastructure.persistence.jpa.ExamBlueprintJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.spring.ExamBlueprintJpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ExamBlueprintRepositoryAdapter implements ExamBlueprintRepositoryPort {
    private final ExamBlueprintJpaRepository jpaRepository;

    public ExamBlueprintRepositoryAdapter(ExamBlueprintJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ExamBlueprint save(ExamBlueprint examBlueprint) {
        ExamBlueprintJpaEntity entity = ExamBlueprintEntityMapper.toEntity(examBlueprint);
        ExamBlueprintJpaEntity saved = jpaRepository.save(entity);
        return ExamBlueprintEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<ExamBlueprint> findById(Long id) {
        return jpaRepository.findById(id).map(ExamBlueprintEntityMapper::toDomain);
    }

    @Override
    public List<ExamBlueprint> findAll() {
        return jpaRepository.findAll().stream()
                .map(ExamBlueprintEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

