package com.timcritt.tfg.infrastructure.web.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialNodeWithAssetsDto {
    private Long id;
    private Long parentNodeId;
    private String kind;
    private String code;
    private String title;
    private Integer displayOrder;
    private String transcriptText;
    private List<MaterialAssetDto> assets;
}
