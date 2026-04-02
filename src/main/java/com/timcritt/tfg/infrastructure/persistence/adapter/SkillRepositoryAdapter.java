package com.timcritt.tfg.infrastructure.persistence.adapter;

import com.timcritt.tfg.application.port.outbound.SkillRepositoryPort;
import com.timcritt.tfg.domain.model.Skill;
import com.timcritt.tfg.infrastructure.persistence.mapper.SkillEntityMapper;
import com.timcritt.tfg.infrastructure.persistence.spring.SkillJpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class SkillRepositoryAdapter implements SkillRepositoryPort {
    private final SkillJpaRepository jpaRepository;

    public SkillRepositoryAdapter(SkillJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(Skill skill) {
        jpaRepository.save(SkillEntityMapper.toEntity(skill));
    }

    @Override
    public Optional<Skill> findById(Long id) {
        return jpaRepository.findById(id).map(SkillEntityMapper::toDomain);
    }

    @Override
    public void delete(Skill skill) {
        jpaRepository.delete(SkillEntityMapper.toEntity(skill));
    }
}
