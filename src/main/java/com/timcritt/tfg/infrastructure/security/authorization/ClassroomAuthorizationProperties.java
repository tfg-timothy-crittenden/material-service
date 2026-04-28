package com.timcritt.tfg.infrastructure.security.authorization;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "authorization.classroom")
public class ClassroomAuthorizationProperties {
    private boolean enabled = false;
    private boolean failOpen = false;
    private String baseUrl = "http://localhost:8080";
    private String checkPath = "/classrooms/internal/authorization/material-access:check";
    private String internalApiKey = "";
    private String internalApiKeyHeader = "X-Internal-Api-Key";
}

