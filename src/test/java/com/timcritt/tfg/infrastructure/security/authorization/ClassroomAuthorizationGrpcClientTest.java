package com.timcritt.tfg.infrastructure.security.authorization;

import com.timcritt.tfg.infrastructure.security.authorization.grpc.AuthorizationCheckRequest;
import com.timcritt.tfg.infrastructure.security.authorization.grpc.AuthorizationCheckResponse;
import com.timcritt.tfg.infrastructure.security.authorization.grpc.ClassroomAuthorizationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClassroomAuthorizationGrpcClientTest {

    private Server server;
    private ManagedChannel channel;

    @AfterEach
    void tearDown() throws IOException {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void checkReadAccess_returnsGrpcResponse() throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new ClassroomAuthorizationServiceGrpc.ClassroomAuthorizationServiceImplBase() {
                    @Override
                    public void checkMaterialAccess(AuthorizationCheckRequest request,
                                                    StreamObserver<AuthorizationCheckResponse> responseObserver) {
                        responseObserver.onNext(AuthorizationCheckResponse.newBuilder()
                                .setAllowed(true)
                                .setReason("assigned")
                                .setEffectiveRole("TEACHER")
                                .build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        ClassroomAuthorizationProperties properties = new ClassroomAuthorizationProperties();
        properties.getGrpc().setDeadlineMillis(1000L);
        ClassroomAuthorizationGrpcClient client = new ClassroomAuthorizationGrpcClient(channel, properties);

        ClassroomAuthorizationPort.MaterialAccessCheckResponse response = client.checkReadAccess("user-7", 15L);

        assertThat(response.isAllowed()).isTrue();
        assertThat(response.reason()).isEqualTo("assigned");
        assertThat(response.effectiveRole()).isEqualTo("TEACHER");
    }

    @Test
    void checkReadAccess_wrapsGrpcFailuresAsUnavailable() throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new ClassroomAuthorizationServiceGrpc.ClassroomAuthorizationServiceImplBase() {
                    @Override
                    public void checkMaterialAccess(AuthorizationCheckRequest request,
                                                    StreamObserver<AuthorizationCheckResponse> responseObserver) {
                        responseObserver.onError(Status.PERMISSION_DENIED.withDescription("denied").asRuntimeException());
                    }
                })
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        ClassroomAuthorizationGrpcClient client = new ClassroomAuthorizationGrpcClient(channel, new ClassroomAuthorizationProperties());

        assertThatThrownBy(() -> client.checkReadAccess("user-7", 15L))
                .isInstanceOf(ClassroomAuthorizationUnavailableException.class)
                .hasMessageContaining("gRPC call failed");
    }
}

