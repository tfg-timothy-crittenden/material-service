package com.timcritt.tfg.infrastructure.security.authorization;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Configuration
@ConditionalOnProperty(prefix = "authorization.classroom", name = "enabled", havingValue = "true")
public class ClassroomAuthorizationGrpcConfig {

    @Bean(destroyMethod = "shutdownNow")
    ManagedChannel classroomAuthorizationManagedChannel(DiscoveryClient discoveryClient,
                                                       ClassroomAuthorizationGrpcProperties properties) {
        ServiceInstance instance = resolveGrpcInstance(discoveryClient, properties.getServiceName());
        Map<String, String> metadata = instance.getMetadata() == null ? Collections.emptyMap() : instance.getMetadata();
        String grpcHost = metadata.getOrDefault("grpc-host", instance.getHost());
        String grpcPortValue = metadata.get("grpc-port");

        int grpcPort;
        try {
            grpcPort = Integer.parseInt(grpcPortValue);
        } catch (NumberFormatException ex) {
            throw new ClassroomAuthorizationUnavailableException(
                    "Invalid grpc-port metadata for classroom-service instance " + instance.getInstanceId()
                            + ": " + grpcPortValue,
                    ex);
        }

        return ManagedChannelBuilder.forAddress(grpcHost, grpcPort)
                .usePlaintext()
                .build();
    }


    private ServiceInstance resolveGrpcInstance(DiscoveryClient discoveryClient, String serviceName) {
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceName);
        if (instances.isEmpty()) {
            throw new ClassroomAuthorizationUnavailableException(
                    "No Eureka instances found for classroom gRPC service '" + serviceName + "'", null);
        }

        return instances.stream()
                .filter(instance -> {
                    Map<String, String> metadata = instance.getMetadata() == null ? Collections.emptyMap() : instance.getMetadata();
                    String grpcEnabled = metadata.get("grpc-enabled");
                    return grpcEnabled == null || Boolean.parseBoolean(grpcEnabled);
                })
                .findFirst()
                .orElseThrow(() -> new ClassroomAuthorizationUnavailableException(
                        "No Eureka instances with grpc-enabled=true found for classroom gRPC service '"
                                + serviceName + "'",
                        null));
    }
}

