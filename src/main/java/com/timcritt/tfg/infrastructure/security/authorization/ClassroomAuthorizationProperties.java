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

}

