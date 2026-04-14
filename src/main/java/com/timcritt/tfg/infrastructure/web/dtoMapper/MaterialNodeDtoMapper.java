package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.domain.model.MaterialNode;
import com.timcritt.tfg.infrastructure.web.dto.MaterialNodeDto;

public final class MaterialNodeDtoMapper {
    private MaterialNodeDtoMapper() {}

    public static MaterialNodeDto toDto(MaterialNode d) {
        if (d == null) return null;
        return MaterialNodeDto.builder()
                .id(d.getId())
                .parentNodeId(d.getParentNodeId())
                .kind(d.getKind())
                .code(d.getCode())
                .title(d.getTitle())
                .displayOrder(d.getDisplayOrder())
                .transcriptText(d.getTranscriptText())
                .config(d.getConfig())
                .build();
    }

    public static MaterialNode toDomain(MaterialNodeDto dto) {
        if (dto == null) return null;
        return MaterialNode.builder()
                .id(dto.getId())
                .parentNodeId(dto.getParentNodeId())
                .kind(dto.getKind())
                .code(dto.getCode())
                .title(dto.getTitle())
                .displayOrder(dto.getDisplayOrder())
                .transcriptText(dto.getTranscriptText())
                .config(dto.getConfig())
                .build();
    }
}
