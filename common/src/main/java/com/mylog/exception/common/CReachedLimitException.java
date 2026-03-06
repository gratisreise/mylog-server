package com.mylog.exception.common;

import com.mylog.exception.BusinessException;
import com.mylog.exception.ErrorCode;

public class CReachedLimitException extends BusinessException {


    public CReachedLimitException(ErrorCode errorCode) {
        super(errorCode);
    }
}
