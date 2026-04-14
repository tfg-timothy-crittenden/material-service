package com.timcritt.tfg.infrastructure.web.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaterialAssetDto {
    public Long id;
    public String kind;
    public String storageKey;
    public Integer displayOrder;
}
