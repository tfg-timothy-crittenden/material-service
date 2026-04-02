package com.timcritt.tfg.infrastructure.service;

import com.timcritt.tfg.application.port.inbound.SkillUseCase;
import com.timcritt.tfg.application.port.outbound.SkillRepositoryPort;
import com.timcritt.tfg.application.service.SkillUseCaseService;
import com.timcritt.tfg.domain.model.Skill;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SkillServiceAdapter implements SkillUseCase {
    private final SkillUseCaseService delegate;

    public SkillServiceAdapter(SkillRepositoryPort repository) {
        this.delegate = new SkillUseCaseService(repository);
    }

    @Override
    @Transactional
    public void createSkill(Skill skill) {
        delegate.createSkill(skill);
    }

    @Override
    @Transactional
    public void deleteSkill(Skill skill) {
        delegate.deleteSkill(skill);
    }

    @Override
    @Transactional(readOnly = true)
    public void findSkillById(Long id) {
        delegate.findSkillById(id);
    }

    @Override
    @Transactional
    public void updateSkill(Skill skill) {
        delegate.updateSkill(skill);
    }
}

