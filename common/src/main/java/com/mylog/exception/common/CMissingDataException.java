package com.mylog.exception.common;

import com.mylog.exception.BusinessException;
import com.mylog.exception.ErrorCode;

public class CMissingDataException extends BusinessException {


    public CMissingDataException(ErrorCode errorCode) {
        super(errorCode);
    }
}
