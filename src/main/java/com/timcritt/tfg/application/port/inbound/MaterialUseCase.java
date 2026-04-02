package com.timcritt.tfg.application.port.inbound;
import com.timcritt.tfg.domain.model.Material;
import java.util.Optional;

public interface MaterialUseCase {
    Material createMaterial(Material material);
    Optional<Material> findMaterialById(Long id);
    Material updateMaterial(Material material);
    Boolean deleteMaterial(Long id);
}

