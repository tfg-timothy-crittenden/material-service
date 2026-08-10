package com.timcritt.tfg.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(requiredProperties = {"id", "parentNodeId", "kind", "title", "displayOrder", "transcriptText", "assets", "config"})
public class MaterialNodeWithAssetsDto {
    @Schema(example = "2001")
    private Long id;

    @Schema(example = "1000")
    private Long parentNodeId;

    @Schema(
            description = "Node type within the material tree",
            allowableValues = {"SECTION", "PART", "ITEM", "QUESTION"},
            example = "ITEM"
    )
    private String kind;

    private String title;

    private Integer displayOrder;

    private String transcriptText;

    private List<MaterialAssetDto> assets;

    @Schema(
            description = "Parsed config object for this node.",
            additionalProperties = Schema.AdditionalPropertiesValue.TRUE,
            example = "{\"timeLimitSeconds\":45,\"responseMode\":\"SPEAK\"}"
    )
    private Map<String, Object> config;
}
