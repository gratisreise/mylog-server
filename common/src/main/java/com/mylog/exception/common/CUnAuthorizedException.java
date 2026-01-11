package com.mylog.exception.common;


import com.mylog.exception.BusinessException;
import com.mylog.exception.ErrorCode;

public class CUnAuthorizedException extends BusinessException {

    public CUnAuthorizedException(ErrorCode errorCode) {
        super(errorCode);
    }
}
