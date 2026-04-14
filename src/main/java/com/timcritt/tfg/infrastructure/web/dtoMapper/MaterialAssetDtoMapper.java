package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.infrastructure.web.dto.MaterialAssetDto;

public final class MaterialAssetDtoMapper {
    private MaterialAssetDtoMapper() {}

    public static MaterialAssetDto toDto(MaterialAsset asset) {
        if (asset == null) return null;
        return MaterialAssetDto.builder()
            .id(asset.getId())
            .kind(asset.getKind() != null ? asset.getKind().name() : null)
            .storageKey(asset.getStorageKey())
            .displayOrder(asset.getDisplayOrder())
            .build();
    }

    // Optionally, add fromDto if needed
}
