package com.i2i.AuthServer.exceptionHandling;

public class DataBaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DataBaseException() {
    }

    public DataBaseException(String message) {
        super(message);
    }

    public DataBaseException(Throwable cause) {
        super(cause);
    }

    public DataBaseException(String message, Throwable cause) {
        super(message, cause);
    }
}
