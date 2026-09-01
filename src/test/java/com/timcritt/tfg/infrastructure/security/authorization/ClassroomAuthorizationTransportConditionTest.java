package com.timcritt.tfg.infrastructure.security.authorization;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class ClassroomAuthorizationTransportConditionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(
                    ClassroomAuthorizationGrpcConfig.class,
                    ClassroomAuthorizationGrpcClient.class,
                    ClassroomAuthorizationGrpcProperties.class,
                    ClassroomAuthorizationProperties.class)
            .withPropertyValues("authorization.classroom.transport=http");

    @Test
    void httpTransportDisablesGrpcAuthorizationBeans() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(ClassroomAuthorizationGrpcConfig.class);
            assertThat(context).doesNotHaveBean(ClassroomAuthorizationPort.class);
        });
    }
}
