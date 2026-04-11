package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.MaterialNode;
import java.util.Optional;
import java.util.List;

public interface MaterialNodeRepositoryPort {
    MaterialNode save(MaterialNode materialNode);
    Optional<MaterialNode> findById(Long id);
    List<MaterialNode> findAll();
    void deleteById(Long id);
    List<MaterialNode> findByParentNodeId(Long parentNodeId);

    List<MaterialNode> findAllDescendantsByRootId(Long sectionId);
    List<MaterialNode> findByKind(String kind);
    List<MaterialNode> findByKindAndExamFamilyIdAndSkillId(String kind, Long examFamilyId, Long skillId);
    Optional<MaterialNode> findByParentIdAndDisplayOrder(Long parentId, Integer displayOrder);
}
