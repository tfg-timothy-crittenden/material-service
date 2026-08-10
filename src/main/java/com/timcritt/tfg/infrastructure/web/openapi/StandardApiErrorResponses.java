package com.timcritt.tfg.infrastructure.web.openapi;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@ApiResponses(value = {
        @ApiResponse(responseCode = "400", description = "Bad Request",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.timcritt.tfg.infrastructure.web.dto.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.timcritt.tfg.infrastructure.web.dto.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "403", description = "Forbidden",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.timcritt.tfg.infrastructure.web.dto.ApiErrorResponse.class))),
        @ApiResponse(responseCode = "503", description = "Service Unavailable",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = com.timcritt.tfg.infrastructure.web.dto.ApiErrorResponse.class)))
})
public @interface StandardApiErrorResponses {
}

