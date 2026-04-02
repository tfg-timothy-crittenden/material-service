package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.MaterialVersion;
import java.util.Optional;

public interface MaterialVersionUseCase {
    MaterialVersion createMaterialVersion(MaterialVersion materialVersion);
    Optional<MaterialVersion> findMaterialVersionById(Long id);
    MaterialVersion updateMaterialVersion(MaterialVersion materialVersion);
    Boolean deleteMaterialVersion(Long id);
}

