package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.application.service.toefl.TOEFLSpeakingNavigationUseCaseService.MaterialNodeWithAssets;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeWithAssetsDto;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeDto;
import com.timcritt.tfg.infrastructure.web.dto.MaterialAssetDto;
import java.util.List;
import java.util.stream.Collectors;

public final class MaterialNodeWithAssetsDtoMapper {
    private MaterialNodeWithAssetsDtoMapper() {}

    public static MaterialNodeWithAssetsDto toDto(MaterialNodeWithAssets src) {
        if (src == null) return null;
        return MaterialNodeWithAssetsDto.builder()
                .id(src.node.getId())
                .parentNodeId(src.node.getParentNodeId())
                .kind(src.node.getKind())
                .code(src.node.getCode())
                .title(src.node.getTitle())
                .displayOrder(src.node.getDisplayOrder())
                .transcriptText(src.node.getTranscriptText())
                .assets(src.assets == null ? null : src.assets.stream().map(MaterialAssetDtoMapper::toDto).collect(Collectors.toList()))
                .build();
    }
}
