package com.timcritt.tfg.infrastructure.security.authorization;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(prefix = "authorization.classroom", name = "transport", havingValue = "grpc")
public class ClassroomAuthorizationGrpcConfig {

    @Bean(destroyMethod = "shutdownNow")
    ManagedChannel classroomAuthorizationManagedChannel(ClassroomAuthorizationProperties properties) {
        ManagedChannelBuilder<?> builder = ManagedChannelBuilder.forTarget(properties.getGrpc().getTarget());
        if (properties.getGrpc().isPlaintext()) {
            builder = builder.usePlaintext();
        }
        return builder.build();
    }
}

