package com.timcritt.tfg.application.dto;

import com.timcritt.tfg.domain.model.MaterialAsset;
import com.timcritt.tfg.domain.model.MaterialNode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class MaterialNodeWithAssetsResult {
    private final MaterialNode node;
    private final List<MaterialAsset> assets;
}

