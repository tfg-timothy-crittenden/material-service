package com.timcritt.tfg.infrastructure.security.authorization;

import com.timcritt.tfg.infrastructure.security.authorization.grpc.AuthorizationCheckRequest;
import com.timcritt.tfg.infrastructure.security.authorization.grpc.AuthorizationCheckResponse;
import com.timcritt.tfg.infrastructure.security.authorization.grpc.ClassroomAuthorizationServiceGrpc;
import io.grpc.Metadata;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ClassroomAuthorizationGrpcClientTest {

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
    void checkReadAccess_returnsGrpcResponse() throws Exception {
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

        ClassroomAuthorizationGrpcProperties properties = new ClassroomAuthorizationGrpcProperties();
        properties.setDeadlineMillis(1000L);
        properties.setInternalApiKey("test-api-key");
        ClassroomAuthorizationGrpcClient client = new ClassroomAuthorizationGrpcClient(channel, properties);

        ClassroomAuthorizationPort.MaterialAccessCheckResponse response = client.checkReadAccess("user-7", 15L);

        assertThat(response.isAllowed()).isTrue();
        assertThat(response.reason()).isEqualTo("assigned");
        assertThat(response.effectiveRole()).isEqualTo("TEACHER");
    }

    @Test
    void checkReadAccess_retriesOnceOnUnavailableThenSucceeds() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        AtomicInteger attempts = new AtomicInteger();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new ClassroomAuthorizationServiceGrpc.ClassroomAuthorizationServiceImplBase() {
                    @Override
                    public void checkMaterialAccess(AuthorizationCheckRequest request,
                                                    StreamObserver<AuthorizationCheckResponse> responseObserver) {
                        if (attempts.incrementAndGet() == 1) {
                            responseObserver.onError(Status.UNAVAILABLE.withDescription("temporary outage").asRuntimeException());
                            return;
                        }
                        responseObserver.onNext(AuthorizationCheckResponse.newBuilder()
                                .setAllowed(true)
                                .setReason("recovered")
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

        ClassroomAuthorizationGrpcProperties properties = new ClassroomAuthorizationGrpcProperties();
        properties.setInternalApiKey("test-api-key");
        ClassroomAuthorizationGrpcClient client = new ClassroomAuthorizationGrpcClient(channel, properties);

        ClassroomAuthorizationPort.MaterialAccessCheckResponse response = client.checkReadAccess("user-7", 15L);

        assertThat(response.isAllowed()).isTrue();
        assertThat(response.reason()).isEqualTo("recovered");
        assertThat(response.effectiveRole()).isEqualTo("TEACHER");
        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void checkReadAccess_whenUnauthenticated_mapsToAccessDenied() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new ClassroomAuthorizationServiceGrpc.ClassroomAuthorizationServiceImplBase() {
                    @Override
                    public void checkMaterialAccess(AuthorizationCheckRequest request,
                                                    StreamObserver<AuthorizationCheckResponse> responseObserver) {
                        responseObserver.onError(Status.UNAUTHENTICATED.withDescription("bad key").asRuntimeException());
                    }
                })
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        ClassroomAuthorizationGrpcProperties properties = new ClassroomAuthorizationGrpcProperties();
        properties.setInternalApiKey("wrong-key");
        ClassroomAuthorizationGrpcClient client = new ClassroomAuthorizationGrpcClient(channel, properties);

        assertThatThrownBy(() -> client.checkReadAccess("user-7", 15L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("Classroom authorization rejected internal credentials");
    }

    @Test
    void checkReadAccess_whenUnavailableTwice_mapsToAuthorizationUnavailable() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        AtomicInteger attempts = new AtomicInteger();
        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(new ClassroomAuthorizationServiceGrpc.ClassroomAuthorizationServiceImplBase() {
                    @Override
                    public void checkMaterialAccess(AuthorizationCheckRequest request,
                                                    StreamObserver<AuthorizationCheckResponse> responseObserver) {
                        attempts.incrementAndGet();
                        responseObserver.onError(Status.UNAVAILABLE.withDescription("down").asRuntimeException());
                    }
                })
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build();

        ClassroomAuthorizationGrpcProperties properties = new ClassroomAuthorizationGrpcProperties();
        properties.setInternalApiKey("test-api-key");
        ClassroomAuthorizationGrpcClient client = new ClassroomAuthorizationGrpcClient(channel, properties);

        assertThatThrownBy(() -> client.checkReadAccess("user-7", 15L))
                .isInstanceOf(ClassroomAuthorizationUnavailableException.class)
                .hasMessageContaining("after retry");

        assertThat(attempts.get()).isEqualTo(2);
    }

    @Test
    void checkReadAccess_attachesInternalApiKeyOnEveryCall() throws Exception {
        String serverName = InProcessServerBuilder.generateName();
        AtomicInteger callCount = new AtomicInteger();
        AtomicReference<String> capturedApiKey = new AtomicReference<>();

        ServerInterceptor capturingInterceptor = new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call,
                                                                        Metadata headers,
                                                                        ServerCallHandler<ReqT, RespT> next) {
                callCount.incrementAndGet();
                capturedApiKey.set(headers.get(Metadata.Key.of("x-internal-api-key", Metadata.ASCII_STRING_MARSHALLER)));
                return next.startCall(call, headers);
            }
        };

        server = InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .intercept(capturingInterceptor)
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

        ClassroomAuthorizationGrpcProperties properties = new ClassroomAuthorizationGrpcProperties();
        properties.setInternalApiKey("test-api-key");
        ClassroomAuthorizationGrpcClient client = new ClassroomAuthorizationGrpcClient(channel, properties);

        client.checkReadAccess("user-7", 15L);
        client.checkReadAccess("user-7", 16L);

        assertThat(callCount.get()).isEqualTo(2);
        assertThat(capturedApiKey.get()).isEqualTo("test-api-key");
    }
}

