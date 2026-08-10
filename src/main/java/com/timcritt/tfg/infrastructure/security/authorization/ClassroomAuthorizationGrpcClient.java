package com.timcritt.tfg.infrastructure.security.authorization;

import com.timcritt.tfg.infrastructure.security.authorization.grpc.AuthorizationCheckRequest;
import com.timcritt.tfg.infrastructure.security.authorization.grpc.AuthorizationCheckResponse;
import com.timcritt.tfg.infrastructure.security.authorization.grpc.ClassroomAuthorizationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "authorization.classroom", name = "transport", havingValue = "grpc")
@RequiredArgsConstructor
public class ClassroomAuthorizationGrpcClient implements ClassroomAuthorizationPort {

    private final ManagedChannel classroomAuthorizationManagedChannel;
    private final ClassroomAuthorizationProperties properties;

    @Override
    public MaterialAccessCheckResponse checkReadAccess(String userId, Long materialId) {
        AuthorizationCheckRequest request = AuthorizationCheckRequest.newBuilder()
                .setUserId(userId)
                .setMaterialId(materialId == null ? 0L : materialId)
                .setAction("READ")
                .build();

        ClassroomAuthorizationServiceGrpc.ClassroomAuthorizationServiceBlockingStub stub =
                ClassroomAuthorizationServiceGrpc.newBlockingStub(classroomAuthorizationManagedChannel);

        if (properties.getGrpc().getDeadlineMillis() > 0) {
            stub = stub.withDeadlineAfter(properties.getGrpc().getDeadlineMillis(), TimeUnit.MILLISECONDS);
        }

        try {
            AuthorizationCheckResponse response = stub.checkMaterialAccess(request);
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
}

