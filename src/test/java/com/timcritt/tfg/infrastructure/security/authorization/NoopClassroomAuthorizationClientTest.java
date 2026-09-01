package com.timcritt.tfg.infrastructure.security.authorization;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NoopClassroomAuthorizationClientTest {

    @Test
    void checkReadAccess_alwaysAllowsWhenAuthorizationDisabled() {
        NoopClassroomAuthorizationClient client = new NoopClassroomAuthorizationClient();

        ClassroomAuthorizationPort.MaterialAccessCheckResponse response = client.checkReadAccess("user-1", 42L);

        assertThat(response.isAllowed()).isTrue();
        assertThat(response.reason()).isEqualTo("authorization disabled");
        assertThat(response.effectiveRole()).isEqualTo("NONE");
    }
}

