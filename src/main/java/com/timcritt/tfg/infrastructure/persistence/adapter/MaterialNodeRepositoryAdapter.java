package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.application.port.outbound.MaterialNodeRepositoryPort;
import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;
import com.timcritt.tfg.infrastructure.persistence.mapper.MaterialNodeEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.spring.MaterialNodeJpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class MaterialNodeRepositoryAdapter implements MaterialNodeRepositoryPort {
    private final MaterialNodeJpaRepository jpaRepository;

    public MaterialNodeRepositoryAdapter(MaterialNodeJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MaterialNode save(MaterialNode materialNode) {
        MaterialNodeJpaEntity entity = MaterialNodeEntityMapper.toEntity(materialNode);
        return MaterialNodeEntityMapper.toDomain(jpaRepository.save(entity));
    }

    @Override
    public Optional<MaterialNode> findById(Long id) {
        return jpaRepository.findById(id).map(MaterialNodeEntityMapper::toDomain);
    }

    @Override
    public List<MaterialNode> findAll() {
        return jpaRepository.findAll().stream().map(MaterialNodeEntityMapper::toDomain).collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<MaterialNode> findByParentNodeId(Long parentNodeId) {
        return jpaRepository.findByParentNodeId(parentNodeId)
                .stream()
                .map(MaterialNodeEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialNode> findAllDescendantsByRootId(Long sectionId) {
        return jpaRepository.findAllDescendantsByRootId(sectionId)
                .stream()
                .map(MaterialNodeEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialNode> findByKind(String kind) {
        return jpaRepository.findByKind(kind)
                .stream()
                .map(MaterialNodeEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<MaterialNode> findByKindAndExamFamilyIdAndSkillId(String kind, Long examFamilyId, Long skillId) {
        return jpaRepository.findByKindAndExamFamilyIdAndSkillId(kind, examFamilyId, skillId)
                .stream()
                .map(MaterialNodeEntityMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<MaterialNode> findByParentIdAndDisplayOrder(Long parentId, Integer displayOrder) {
        return jpaRepository.findByParentNodeIdAndDisplayOrder(parentId, displayOrder)
                .map(MaterialNodeEntityMapper::toDomain);
    }
}
