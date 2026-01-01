package com.mylog.exception;


import com.mylog.enums.ErrorMessage;

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

    public CUnAuthorizedException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }
}
