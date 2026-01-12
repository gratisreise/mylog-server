package com.mylog.exception.common;

import com.mylog.exception.BusinessException;
import com.mylog.exception.auth.AuthError;

public class CDuplicatedException extends BusinessException {


    public CDuplicatedException(AuthError commonError) {
        super(commonError);
    }
}
