package com.timcritt.tfg.infrastructure.security.authorization;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "classroom.authorization")
public class ClassroomAuthorizationGrpcProperties {
    private String serviceName = "classroom-service";
    private String internalApiKey = "";
    private long deadlineMillis = 1500L;
}

