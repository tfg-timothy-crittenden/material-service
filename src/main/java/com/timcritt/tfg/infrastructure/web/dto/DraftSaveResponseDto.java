package com.timcritt.tfg.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(requiredProperties = {"materialId"})
public class DraftSaveResponseDto {
    @Schema(example = "123")
    private Long materialId;
}

