package com.mylog.exception;

import com.mylog.enums.ErrorMessage;

public class ReachedLimitException extends RuntimeException {

    public ReachedLimitException(String message) {
        super(message);
    }

    public ReachedLimitException(ErrorMessage message) {
        super(message.getMessage());
    }
}
