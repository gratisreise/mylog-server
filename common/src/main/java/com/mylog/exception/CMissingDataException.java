package com.mylog.exception;

import com.mylog.enums.ErrorMessage;
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

    public CMissingDataException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
    }
}
