package com.i2i.AuthServer.exceptionHandling;

public class UserAlreadyPresent extends RuntimeException {

    public UserAlreadyPresent() {
    }

    public UserAlreadyPresent(String message) {
        super(message);
    }

    public UserAlreadyPresent(Throwable cause) {
        super(cause);
    }

    public UserAlreadyPresent(String message, Throwable cause) {
        super(message, cause);
    }
}
