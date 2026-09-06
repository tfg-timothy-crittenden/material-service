package com.timcritt.tfg.application.port.outbound;

import com.timcritt.tfg.domain.model.MaterialAsset;
import java.util.List;
import java.util.Optional;

public interface MaterialAssetRepositoryPort {
    Optional<MaterialAsset> findById(Long id);
    List<MaterialAsset> findByMaterialNodeId(Long materialNodeId);
    MaterialAsset save(MaterialAsset asset);
    void deleteById(Long id);

    

}

