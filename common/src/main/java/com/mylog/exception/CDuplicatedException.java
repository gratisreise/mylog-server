package com.mylog.exception;

import com.mylog.enums.ErrorMessage;

public class CDuplicatedException extends RuntimeException {

    public CDuplicatedException(String message) {
        super(message);
    }
    public CDuplicatedException(ErrorMessage message) {
        super(message.getMessage());
    }

    public CDuplicatedException(String message, Throwable cause) {
        super(message, cause);
    }
    public CDuplicatedException(Throwable cause) {
        super(cause);
    }
}
