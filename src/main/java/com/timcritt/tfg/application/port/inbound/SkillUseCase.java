package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.Skill;

public interface SkillUseCase {
    void createSkill(Skill skill);
    void deleteSkill(Skill skill);
    void findSkillById(Long id);
    void updateSkill(Skill skill);

}
