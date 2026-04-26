package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.application.dto.MaterialNodeWithAssetsResult;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeWithAssetsDto;
import java.util.stream.Collectors;

public final class MaterialNodeWithAssetsDtoMapper {
    private MaterialNodeWithAssetsDtoMapper() {}

    public static MaterialNodeWithAssetsDto toDto(MaterialNodeWithAssetsResult src) {
        if (src == null) return null;
        return MaterialNodeWithAssetsDto.builder()
                .id(src.getNode().getId())
                .parentNodeId(src.getNode().getParentNodeId())
                .kind(src.getNode().getKind())
                .title(src.getNode().getTitle())
                .displayOrder(src.getNode().getDisplayOrder())
                .transcriptText(src.getNode().getTranscriptText())
                .config(src.getNode().getConfig())
                .assets(src.getAssets() == null ? null : src.getAssets().stream().map(MaterialAssetDtoMapper::toDto).collect(Collectors.toList()))
                .build();
    }
}
