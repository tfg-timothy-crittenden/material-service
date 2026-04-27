package com.timcritt.tfg.infrastructure.security.authorization;

public class ClassroomAuthorizationUnavailableException extends RuntimeException {
    public ClassroomAuthorizationUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}

