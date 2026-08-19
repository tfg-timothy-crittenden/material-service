package com.timcritt.tfg.infrastructure.security.authorization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaterialReadAuthorizationService {

    private final ObjectProvider<ClassroomAuthorizationPort> authorizationClientProvider;
    private final ClassroomAuthorizationProperties properties;

    public void assertCanRead(Long materialId) {
        if (!properties.isEnabled()) {
            return;
        }
        if (materialId == null) {
            throw new IllegalArgumentException("materialId is required for authorization");
        }

        String userId = resolveCurrentUserId();
        ClassroomAuthorizationPort authorizationClient = authorizationClientProvider.getIfAvailable();
        if (authorizationClient == null) {
            throw new ClassroomAuthorizationUnavailableException(
                    "Classroom authorization is enabled but no authorization transport client is configured",
                    null);
        }

        try {
            ClassroomAuthorizationPort.MaterialAccessCheckResponse response =
                    authorizationClient.checkReadAccess(userId, materialId);
            if (!response.isAllowed()) {
                throw new AccessDeniedException("Access denied to material " + materialId);
            }
        } catch (ClassroomAuthorizationUnavailableException ex) {
            if (properties.isFailOpen()) {
                log.warn("Classroom authorization unavailable for material {} and user {}. Allowing request because fail-open is enabled.",
                        materialId, userId, ex);
                return;
            }
            throw ex;
        }
    }

    private String resolveCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object claimValue = jwt.getClaim("userId");
            if (claimValue != null) {
                String userId = String.valueOf(claimValue).trim();
                if (!userId.isEmpty()) {
                    return userId;
                }
            }
        }

        if (authentication != null && authentication.isAuthenticated()
                && authentication.getName() != null
                && !"anonymousUser".equalsIgnoreCase(authentication.getName())) {
            return authentication.getName();
        }

        throw new UnauthenticatedUserException("Unable to resolve user identity from authenticated token");
    }
}
