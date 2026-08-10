package com.timcritt.tfg.infrastructure.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(requiredProperties = {"message", "errors"})
public class ApiErrorResponse {
    @Schema(description = "Human-readable error summary", example = "Validation failed")
    private String message;

    @Schema(
            description = "Optional field-level errors keyed by field name",
            type = "object",
            example = "{\"materialTitle\":\"must not be blank\"}"
    )
    private Map<String, String> errors;
}

