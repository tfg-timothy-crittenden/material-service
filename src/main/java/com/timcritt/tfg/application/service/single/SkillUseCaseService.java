package com.timcritt.tfg.application.service.single;

import com.timcritt.tfg.application.port.inbound.SkillUseCase;
import com.timcritt.tfg.application.port.outbound.SkillRepositoryPort;
import com.timcritt.tfg.domain.model.Skill;
import java.util.Optional;

public class SkillUseCaseService implements SkillUseCase {
    private final SkillRepositoryPort repository;

    public SkillUseCaseService(SkillRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public void createSkill(Skill skill) {
        repository.save(skill);
    }

    @Override
    public void deleteSkill(Skill skill) {
        repository.delete(skill);
    }

    @Override
    public void findSkillById(Long id) {
        Optional<Skill> skill = repository.findById(id);
        skill.orElseThrow();
    }

    @Override
    public void updateSkill(Skill skill) {
        repository.save(skill);
    }
}
