package com.mylog.exception.auth;

import com.mylog.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AuthError implements ErrorCode {
    INVALID_LOGIN_INPUT("AUTH001", "아이디가 틀렸습니다."),
    PASSWORD_MISMATCH("AUTH001", "비밀번호가 틀렸습니다."),
    DUPLICATED_EMAIL("AUTH002", "이미 가입된 유저입니다."),

    ;

    private final String code;
    private final String message;
}
