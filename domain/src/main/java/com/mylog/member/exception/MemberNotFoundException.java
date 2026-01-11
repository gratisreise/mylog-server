package com.mylog.member.exception;

import com.mylog.exception.auth.AuthError;
import com.mylog.exception.BusinessException;
import com.mylog.exception.ErrorCode;

public class MemberNotFoundException extends BusinessException {

    public MemberNotFoundException(ErrorCode errorCode){
        super(errorCode);
    }

    public MemberNotFoundException() {
        super(AuthError.INVALID_LOGIN_INPUT);
    }
}
