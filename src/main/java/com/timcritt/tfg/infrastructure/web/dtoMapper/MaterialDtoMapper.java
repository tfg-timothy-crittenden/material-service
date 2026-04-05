package com.timcritt.tfg.infrastructure.web.dtoMapper;
import com.timcritt.tfg.domain.model.Material;
import com.timcritt.tfg.infrastructure.web.dto.MaterialDto;

public final class MaterialDtoMapper {
    private MaterialDtoMapper() {}
    public static MaterialDto toDto(Material d) {
        if (d == null) return null;
        return MaterialDto.builder()
                .id(d.getId())
                .examFamilyId(d.getExamFamilyId())
                .blueprintId(d.getBlueprintId())
                .materialNodeId(d.getMaterialNodeId())
                .code(d.getCode())
                .title(d.getTitle())
                .description(d.getDescription())
                .authorId(d.getAuthorId())
                .ownerOrgId(d.getOwnerOrgId())
                .version(d.getVersion())
                .createdAt(d.getCreatedAt())
                .updatedAt(d.getUpdatedAt())
                .build();
    }
    public static Material toDomain(MaterialDto dto) {
        if (dto == null) return null;
        return Material.builder()
                .id(dto.getId())
                .examFamilyId(dto.getExamFamilyId())
                .blueprintId(dto.getBlueprintId())
                .materialNodeId(dto.getMaterialNodeId())
                .code(dto.getCode())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .authorId(dto.getAuthorId())
                .ownerOrgId(dto.getOwnerOrgId())
                .version(dto.getVersion())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }
}
