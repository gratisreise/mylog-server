package com.mylog.exception;

public class CReachedLimitException extends RuntimeException {

    public CReachedLimitException(String message) {
        super(message);
    }
    public CReachedLimitException(String message, Throwable cause) {
        super(message, cause);
    }
    public CReachedLimitException(Throwable cause) {
        super(cause);
    }
}
