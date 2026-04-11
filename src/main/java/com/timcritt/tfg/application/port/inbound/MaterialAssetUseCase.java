package com.timcritt.tfg.application.port.inbound;

import com.timcritt.tfg.domain.model.MaterialAsset;
import java.util.List;
import java.util.Optional;

public interface MaterialAssetUseCase {
    Optional<MaterialAsset> getById(Long id);
    MaterialAsset save(MaterialAsset asset);
    List<MaterialAsset> findByMaterialNodeId(Long materialNodeId);
    // Add more use case methods as needed
}
