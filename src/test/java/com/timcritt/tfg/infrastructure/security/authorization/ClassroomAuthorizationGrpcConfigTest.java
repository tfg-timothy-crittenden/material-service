package com.timcritt.tfg.infrastructure.security.authorization;

import com.timcritt.tfg.infrastructure.security.authorization.grpc.AuthorizationCheckRequest;
import com.timcritt.tfg.infrastructure.security.authorization.grpc.AuthorizationCheckResponse;
import com.timcritt.tfg.infrastructure.security.authorization.grpc.ClassroomAuthorizationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ClassroomAuthorizationGrpcConfigTest {

    private Server server;
    private ManagedChannel channel;

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void classroomAuthorizationManagedChannel_usesDiscoveryMetadataForHostAndPort() throws Exception {
        server = NettyServerBuilder.forPort(0)
                .directExecutor()
                .addService(new ClassroomAuthorizationServiceGrpc.ClassroomAuthorizationServiceImplBase() {
                    @Override
                    public void checkMaterialAccess(AuthorizationCheckRequest request,
                                                    StreamObserver<AuthorizationCheckResponse> responseObserver) {
                        responseObserver.onNext(AuthorizationCheckResponse.newBuilder()
                                .setAllowed(true)
                                .setReason("ok")
                                .setEffectiveRole("TEACHER")
                                .build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();

        int grpcPort = server.getPort();
        DefaultServiceInstance instance = new DefaultServiceInstance(
                "classroom-service-1",
                "classroom-service",
                "host.docker.internal",
                grpcPort,
                false);
        instance.getMetadata().put("grpc-enabled", "true");
        instance.getMetadata().put("grpc-host", "127.0.0.1");
        instance.getMetadata().put("grpc-port", String.valueOf(grpcPort));

        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        when(discoveryClient.getInstances("classroom-service")).thenReturn(List.of(instance));

        ClassroomAuthorizationGrpcProperties properties = new ClassroomAuthorizationGrpcProperties();
        properties.setServiceName("classroom-service");

        ClassroomAuthorizationGrpcConfig config = new ClassroomAuthorizationGrpcConfig();
        channel = config.classroomAuthorizationManagedChannel(discoveryClient, properties);

        AuthorizationCheckResponse response = ClassroomAuthorizationServiceGrpc.newBlockingStub(channel)
                .checkMaterialAccess(AuthorizationCheckRequest.newBuilder()
                        .setUserId("user-1")
                        .setMaterialId(42L)
                        .setAction("READ")
                        .build());

        assertThat(response.getAllowed()).isTrue();
        assertThat(response.getReason()).isEqualTo("ok");
        verify(discoveryClient).getInstances("classroom-service");
    }

    @Test
    void classroomAuthorizationManagedChannel_fallsBackToInstanceHostWhenGrpcHostMissing() throws Exception {
        server = NettyServerBuilder.forPort(0)
                .directExecutor()
                .addService(new ClassroomAuthorizationServiceGrpc.ClassroomAuthorizationServiceImplBase() {
                    @Override
                    public void checkMaterialAccess(AuthorizationCheckRequest request,
                                                    StreamObserver<AuthorizationCheckResponse> responseObserver) {
                        responseObserver.onNext(AuthorizationCheckResponse.newBuilder()
                                .setAllowed(true)
                                .setReason("fallback-host")
                                .setEffectiveRole("TEACHER")
                                .build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();

        int grpcPort = server.getPort();
        DefaultServiceInstance instance = new DefaultServiceInstance(
                "classroom-service-1",
                "classroom-service",
                "127.0.0.1",
                grpcPort,
                false);
        instance.getMetadata().put("grpc-enabled", "true");
        instance.getMetadata().put("grpc-port", String.valueOf(grpcPort));

        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        when(discoveryClient.getInstances("classroom-service")).thenReturn(List.of(instance));

        ClassroomAuthorizationGrpcProperties properties = new ClassroomAuthorizationGrpcProperties();
        properties.setServiceName("classroom-service");

        ClassroomAuthorizationGrpcConfig config = new ClassroomAuthorizationGrpcConfig();
        channel = config.classroomAuthorizationManagedChannel(discoveryClient, properties);

        AuthorizationCheckResponse response = ClassroomAuthorizationServiceGrpc.newBlockingStub(channel)
                .checkMaterialAccess(AuthorizationCheckRequest.newBuilder()
                        .setUserId("user-1")
                        .setMaterialId(42L)
                        .setAction("READ")
                        .build());

        assertThat(response.getAllowed()).isTrue();
        assertThat(response.getReason()).isEqualTo("fallback-host");
    }

    @Test
    void classroomAuthorizationManagedChannel_throwsWhenGrpcMetadataIsMissing() {
        DefaultServiceInstance instance = new DefaultServiceInstance(
                "classroom-service-1",
                "classroom-service",
                "host.docker.internal",
                9093,
                false);
        instance.getMetadata().put("grpc-enabled", "true");
        instance.getMetadata().put("grpc-host", "127.0.0.1");

        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        when(discoveryClient.getInstances("classroom-service")).thenReturn(List.of(instance));

        ClassroomAuthorizationGrpcProperties properties = new ClassroomAuthorizationGrpcProperties();
        properties.setServiceName("classroom-service");

        ClassroomAuthorizationGrpcConfig config = new ClassroomAuthorizationGrpcConfig();

        assertThatThrownBy(() -> config.classroomAuthorizationManagedChannel(discoveryClient, properties))
                .isInstanceOf(ClassroomAuthorizationUnavailableException.class)
                .hasMessageContaining("Invalid grpc-port metadata");
    }

    @Test
    void classroomAuthorizationManagedChannel_throwsWhenNoInstancesAreDiscovered() {
        DiscoveryClient discoveryClient = mock(DiscoveryClient.class);
        when(discoveryClient.getInstances("classroom-service")).thenReturn(List.of());

        ClassroomAuthorizationGrpcProperties properties = new ClassroomAuthorizationGrpcProperties();
        properties.setServiceName("classroom-service");

        ClassroomAuthorizationGrpcConfig config = new ClassroomAuthorizationGrpcConfig();

        assertThat(org.assertj.core.api.Assertions.catchThrowable(() ->
                config.classroomAuthorizationManagedChannel(discoveryClient, properties)))
                .isInstanceOf(ClassroomAuthorizationUnavailableException.class)
                .hasMessageContaining("No Eureka instances found for classroom gRPC service");
    }
}

