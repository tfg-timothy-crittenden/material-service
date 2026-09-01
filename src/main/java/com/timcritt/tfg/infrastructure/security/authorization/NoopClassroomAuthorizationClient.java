package com.timcritt.tfg.infrastructure.security.authorization;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "authorization.classroom", name = "enabled", havingValue = "false", matchIfMissing = true)
public class NoopClassroomAuthorizationClient implements ClassroomAuthorizationPort {

    @Override
    public MaterialAccessCheckResponse checkReadAccess(String userId, Long materialId) {
        return new MaterialAccessCheckResponse(true, "authorization disabled", "NONE");
    }
}

