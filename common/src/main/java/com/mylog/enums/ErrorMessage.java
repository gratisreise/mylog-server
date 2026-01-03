package com.mylog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorMessage {
    NOT_YOUR_ARTICLE("게시글에 대한 권한이 없습니다."),
    INVALID_PASSWORD("비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."),
    NOT_YOUR_ACCOUNT("계정에 대한 권한이 없습니다.")
    ;
    private String message;
}
