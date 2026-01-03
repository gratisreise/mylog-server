package com.mylog.exception;

import com.mylog.enums.ErrorMessage;

public class CUnDeletedException extends RuntimeException {

    public CUnDeletedException(String message) {
        super(message);
    }

    public CUnDeletedException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }
}
