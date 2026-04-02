package com.timcritt.tfg.infrastructure.web.dtoMapper;

import com.timcritt.tfg.domain.model.MaterialVersion;
import com.timcritt.tfg.infrastructure.web.dto.MaterialVersionDto;

public final class MaterialVersionDtoMapper {
    private MaterialVersionDtoMapper() {}
    public static MaterialVersionDto toDto(MaterialVersion d) {
        if (d == null) return null;
        return MaterialVersionDto.builder()
                .id(d.getId())
                .materialId(d.getMaterialId())
                .versionNo(d.getVersionNo())
                .status(d.getStatus())
                .changeSummary(d.getChangeSummary())
                .createdBy(d.getCreatedBy())
                .publishedAt(d.getPublishedAt())
                .blueprintSnapshot(d.getBlueprintSnapshot())
                .isLocked(d.getIsLocked())
                .version(d.getVersion())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
    public static MaterialVersion toDomain(MaterialVersionDto dto) {
        if (dto == null) return null;
        return MaterialVersion.builder()
                .id(dto.getId())
                .materialId(dto.getMaterialId())
                .versionNo(dto.getVersionNo())
                .status(dto.getStatus())
                .changeSummary(dto.getChangeSummary())
                .createdBy(dto.getCreatedBy())
                .publishedAt(dto.getPublishedAt())
                .blueprintSnapshot(dto.getBlueprintSnapshot())
                .isLocked(dto.getIsLocked())
                .version(dto.getVersion())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}

