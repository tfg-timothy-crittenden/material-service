package com.timcritt.tfg.infrastructure.persistence.spring;

import com.timcritt.tfg.infrastructure.persistence.jpa.MaterialNodeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface MaterialNodeJpaRepository extends JpaRepository<MaterialNodeJpaEntity, Long> {
    List<MaterialNodeJpaEntity> findByParentNodeId(Long parentNodeId);

    // Recursive CTE to get all descendants of a node (including the root)
    //Can be used to search from any point in the material_node tree
    @Query(value = """
        WITH RECURSIVE descendants AS (
            SELECT * FROM material_node WHERE id = :rootId
            UNION ALL
            SELECT mn.* FROM material_node mn
            INNER JOIN descendants d ON mn.parent_node_id = d.id
        )
        SELECT * FROM descendants;
    """, nativeQuery = true)
    List<MaterialNodeJpaEntity> findAllDescendantsByRootId(@Param("rootId") Long rootId);

    List<MaterialNodeJpaEntity> findByKind(String kind);

    @Query(value = """
        SELECT mn.* FROM material_node mn
        JOIN material m ON mn.id = m.material_node_id
        WHERE m.exam_family_id = :examFamilyId
          AND mn.kind = :kind
          AND (:skillId IS NULL OR mn.skill_id = :skillId)
    """, nativeQuery = true)
    List<MaterialNodeJpaEntity> findByKindAndExamFamilyIdAndSkillId(@Param("kind") String kind, @Param("examFamilyId") Long examFamilyId, @Param("skillId") Long skillId);
}
