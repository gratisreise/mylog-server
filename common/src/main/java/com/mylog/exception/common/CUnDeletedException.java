package com.mylog.exception.common;

import com.mylog.exception.BusinessException;
import com.mylog.exception.ErrorCode;

public class CUnDeletedException extends BusinessException {

    public CUnDeletedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
