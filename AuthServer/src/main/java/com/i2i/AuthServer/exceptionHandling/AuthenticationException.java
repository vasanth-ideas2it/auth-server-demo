package com.i2i.AuthServer.exceptionHandling;

public class AuthenticationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthenticationException() {
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
