package com.mylog.common.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class CMissingDataException extends RuntimeException {

    public CMissingDataException(String message) {
        super(message);
    }

    public CMissingDataException(String message, Throwable cause) {
        super(message, cause);
    }
    public CMissingDataException(Throwable cause) {
        super(cause);
    }
}
