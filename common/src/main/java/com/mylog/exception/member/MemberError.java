package com.mylog.exception.member;

import com.mylog.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberError implements ErrorCode {

    MEMBER_NOT_FOUND("M001", "존재하지 않는 회원입니다."),
    ;

    private final String code;
    private final String message;

}
