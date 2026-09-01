package com.timcritt.tfg.infrastructure.security.authorization;

import com.timcritt.tfg.infrastructure.security.authorization.grpc.AuthorizationCheckRequest;
import com.timcritt.tfg.infrastructure.security.authorization.grpc.AuthorizationCheckResponse;
import com.timcritt.tfg.infrastructure.security.authorization.grpc.ClassroomAuthorizationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "authorization.classroom", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class ClassroomAuthorizationGrpcClient implements ClassroomAuthorizationPort {

    private final ManagedChannel classroomAuthorizationManagedChannel;
    private final ClassroomAuthorizationGrpcProperties properties;

    @Override
    public MaterialAccessCheckResponse checkReadAccess(String userId, Long materialId) {
        AuthorizationCheckRequest request = AuthorizationCheckRequest.newBuilder()
                .setUserId(userId)
                .setMaterialId(materialId == null ? 0L : materialId)
                .setAction("READ")
                .build();

        try {
            AuthorizationCheckResponse response = invokeWithRetry(request);
            if (response == null) {
                throw new ClassroomAuthorizationUnavailableException("Authorization service returned empty gRPC response", null);
            }
            return new MaterialAccessCheckResponse(
                    response.getAllowed(),
                    response.getReason(),
                    response.getEffectiveRole());
        } catch (StatusRuntimeException ex) {
            throw new ClassroomAuthorizationUnavailableException(
                    "Classroom authorization gRPC call failed: " + ex.getStatus(), ex);
        }
    }

    private AuthorizationCheckResponse invokeWithRetry(AuthorizationCheckRequest request) {
        try {
            return invokeOnce(request);
        } catch (StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == Status.Code.UNAUTHENTICATED) {
                log.warn("Classroom authorization gRPC rejected internal credentials for user {} and material {}: {}",
                        request.getUserId(), request.getMaterialId(), ex.getStatus());
                throw new org.springframework.security.access.AccessDeniedException(
                        "Classroom authorization rejected internal credentials");
            }

            if (ex.getStatus().getCode() != Status.Code.UNAVAILABLE) {
                throw ex;
            }

            log.warn("Classroom authorization gRPC unavailable for user {} and material {}: {}. Retrying once.",
                    request.getUserId(), request.getMaterialId(), ex.getStatus());

            try {
                return invokeOnce(request);
            } catch (StatusRuntimeException retryEx) {
                throw new ClassroomAuthorizationUnavailableException(
                        "Classroom authorization gRPC call failed after retry: " + retryEx.getStatus(),
                        retryEx);
            }
        }
    }

    private AuthorizationCheckResponse invokeOnce(AuthorizationCheckRequest request) {
        Metadata headers = new Metadata();
        Metadata.Key<String> headerKey = Metadata.Key.of("x-internal-api-key", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(headerKey, properties.getInternalApiKey() == null ? "" : properties.getInternalApiKey());

        ClassroomAuthorizationServiceGrpc.ClassroomAuthorizationServiceBlockingStub stub =
                ClassroomAuthorizationServiceGrpc.newBlockingStub(classroomAuthorizationManagedChannel)
                        .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));

        if (properties.getDeadlineMillis() > 0) {
            stub = stub.withDeadlineAfter(properties.getDeadlineMillis(), TimeUnit.MILLISECONDS);
        }
        return stub.checkMaterialAccess(request);
    }
}

