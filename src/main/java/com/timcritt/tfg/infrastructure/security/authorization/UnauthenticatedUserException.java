package com.timcritt.tfg.infrastructure.security.authorization;

public class UnauthenticatedUserException extends RuntimeException {
    public UnauthenticatedUserException(String message) {
        super(message);
    }
}

