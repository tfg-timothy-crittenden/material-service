package com.timcritt.tfg.infrastructure.security.authorization;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MaterialReadAuthorizationServiceTest {

    private final ClassroomAuthorizationPort authorizationPort = mock(ClassroomAuthorizationPort.class);
    private final ObjectProvider<ClassroomAuthorizationPort> authorizationPortProvider =
            new StaticListableBeanFactory(java.util.Map.of("authorizationPort", authorizationPort))
                    .getBeanProvider(ClassroomAuthorizationPort.class);
    private final ClassroomAuthorizationProperties properties = new ClassroomAuthorizationProperties();
    private final MaterialReadAuthorizationService service = new MaterialReadAuthorizationService(authorizationPortProvider, properties);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void assertCanRead_usesJwtUserIdAndDelegatesToActivePort() {
        properties.setEnabled(true);
        SecurityContextHolder.getContext().setAuthentication(jwtAuthentication());
        when(authorizationPort.checkReadAccess("user-123", 15L))
                .thenReturn(new ClassroomAuthorizationPort.MaterialAccessCheckResponse(true, null, "STUDENT"));

        service.assertCanRead(15L);

        verify(authorizationPort, times(1)).checkReadAccess("user-123", 15L);
    }

    @Test
    void assertCanRead_whenAuthorizationServiceUnavailableAndFailOpen_allowsRequest() {
        properties.setEnabled(true);
        properties.setFailOpen(true);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser());
        doThrow(new ClassroomAuthorizationUnavailableException("grpc down", null))
                .when(authorizationPort).checkReadAccess("user-123", 15L);

        assertThatCode(() -> service.assertCanRead(15L)).doesNotThrowAnyException();
        verify(authorizationPort, times(1)).checkReadAccess("user-123", 15L);
    }

    @Test
    void assertCanRead_whenDenied_throwsAccessDeniedException() {
        properties.setEnabled(true);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser());
        when(authorizationPort.checkReadAccess(eq("user-123"), eq(15L)))
                .thenReturn(new ClassroomAuthorizationPort.MaterialAccessCheckResponse(false, "not assigned", "NONE"));

        assertThatThrownBy(() -> service.assertCanRead(15L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Access denied to material 15");
    }

    @Test
    void assertCanRead_whenEnabledButNoAuthorizationClientConfigured_throwsUnavailable() {
        MaterialReadAuthorizationService serviceWithoutClient = new MaterialReadAuthorizationService(
                new StaticListableBeanFactory().getBeanProvider(ClassroomAuthorizationPort.class),
                properties);
        properties.setEnabled(true);
        SecurityContextHolder.getContext().setAuthentication(authenticatedUser());

        assertThatThrownBy(() -> serviceWithoutClient.assertCanRead(15L))
                .isInstanceOf(ClassroomAuthorizationUnavailableException.class)
                .hasMessageContaining("no authorization transport client is configured");
    }

    private static JwtAuthenticationToken jwtAuthentication() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("userId", "user-123")
                .build();
        return new JwtAuthenticationToken(jwt);
    }

    private static UsernamePasswordAuthenticationToken authenticatedUser() {
        return new UsernamePasswordAuthenticationToken("user-123", "n/a", java.util.List.of());
    }
}
