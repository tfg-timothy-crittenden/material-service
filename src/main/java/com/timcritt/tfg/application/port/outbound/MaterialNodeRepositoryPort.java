package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.domain.model.MaterialNodeKind;

import java.util.Optional;
import java.util.List;

public interface MaterialNodeRepositoryPort {
    MaterialNode save(MaterialNode materialNode);
    Optional<MaterialNode> findById(Long id);
    List<MaterialNode> findAll();
    void deleteById(Long id);
    List<MaterialNode> findByParentNodeId(Long parentNodeId);
    List<MaterialNode> findByKind(MaterialNodeKind kind);

    List<MaterialNode> findByKindAndExamFamilyIdAndSkillId(
            MaterialNodeKind kind,
            Long examFamilyId,
            Long skillId
    );
    Optional<MaterialNode> findByParentIdAndDisplayOrder(Long parentId, Integer displayOrder);

    //Refactor


}
