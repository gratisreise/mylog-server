package com.mylog.exception;


public class CUnAuthorizedException extends RuntimeException{
    public CUnAuthorizedException(String message) {
        super(message);
    }

    public CUnAuthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
    public CUnAuthorizedException(Throwable cause) {
        super(cause);
    }
}
