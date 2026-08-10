package com.timcritt.tfg.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(requiredProperties = {"id", "kind", "storageKey", "displayOrder"})
public class MaterialAssetDto {
    @Schema(description = "Material asset identifier", example = "101")
    public Long id;

    @Schema(
            description = "Asset category",
            allowableValues = {"TEXT", "AUDIO", "IMAGE", "VIDEO", "PDF", "OTHER"},
            example = "AUDIO"
    )
    public String kind;

    @Schema(description = "Storage key/path used by object storage", example = "materials/audio/q1.mp3")
    public String storageKey;

    @Schema(description = "Display order relative to sibling assets", example = "1")
    public Integer displayOrder;
}
