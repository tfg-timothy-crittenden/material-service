package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.MaterialVersion;
import java.util.Optional;

public interface MaterialVersionRepositoryPort {
    MaterialVersion save(MaterialVersion materialVersion);
    Optional<MaterialVersion> findById(Long id);
    Boolean delete(Long id);
}

