package com.timcritt.tfg.infrastructure.web.dto;

import lombok.*;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialNodeDto {
    private Long id;
    private Long parentNodeId;
    private String kind;
    private String title;
    private Integer displayOrder;
    private String transcriptText;
    private Map<String, Object> config;
    private List<MaterialAssetDto> materialAssets;
}
