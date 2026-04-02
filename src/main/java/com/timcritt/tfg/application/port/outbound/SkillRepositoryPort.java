package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.Skill;

import java.util.Optional;

public interface SkillRepositoryPort {
    void save(Skill skill);
    Optional<Skill> findById(Long id);
    void delete(Skill skill);
}
