package com.timcritt.tfg.infrastructure.security.authorization;

public interface ClassroomAuthorizationPort {

    MaterialAccessCheckResponse checkReadAccess(String userId, Long materialId);

    record MaterialAccessCheckRequest(String userId, Long materialId, String action) {
    }

    record MaterialAccessCheckResponse(Boolean allowed, String reason, String effectiveRole) {
        public boolean isAllowed() {
            return Boolean.TRUE.equals(allowed);
        }
    }
}

