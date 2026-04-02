package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.application.port.outbound.ExamBlueprintNodeRepositoryPort;
import com.timcritt.tfg.domain.model.ExamBlueprintNode;
import com.timcritt.tfg.infrastructure.persistence.jpa.ExamBlueprintNodeJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.mapper.ExamBlueprintNodeEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.spring.ExamBlueprintNodeJpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ExamBlueprintNodeRepositoryAdapter implements ExamBlueprintNodeRepositoryPort {
    private final ExamBlueprintNodeJpaRepository jpaRepository;

    public ExamBlueprintNodeRepositoryAdapter(ExamBlueprintNodeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ExamBlueprintNode save(ExamBlueprintNode node) {
        ExamBlueprintNodeJpaEntity entity = ExamBlueprintNodeEntityMapper.toEntity(node);
        ExamBlueprintNodeJpaEntity saved = jpaRepository.save(entity);
        return ExamBlueprintNodeEntityMapper.toDomain(saved);
    }

    @Override
    public Optional<ExamBlueprintNode> findById(Long id) {
        return jpaRepository.findById(id).map(ExamBlueprintNodeEntityMapper::toDomain);
    }

    @Override
    public List<ExamBlueprintNode> findAll() {
        return jpaRepository.findAll().stream()
                .map(ExamBlueprintNodeEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }
}

