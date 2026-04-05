package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.Material;
import java.util.List;
import java.util.Optional;

public interface MaterialRepositoryPort {
    Material save(Material material);
    Optional<Material> findById(Long id);
    Boolean delete(Long id);
    List<Material> findByExamFamilyId(Long examFamilyId);
}
