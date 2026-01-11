package com.mylog.exception.common;


import com.mylog.exception.BusinessException;
import com.mylog.exception.ErrorCode;

public class CInvalidDataException extends BusinessException {

    public CInvalidDataException(ErrorCode errorCode) {
        super(errorCode);
    }
}
