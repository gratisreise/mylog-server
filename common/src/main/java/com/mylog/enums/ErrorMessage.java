package com.mylog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorMessage {
    NOT_YOUR_ARTICLE("게시글에 대한 권한이 없습니다."),
    INVALID_PASSWORD("비밀번호는 영문, 숫자, 특수문자를 포함하여 8자 이상이어야 합니다."),
    NOT_YOUR_ACCOUNT("계정에 대한 권한이 없습니다."),
    UNDELTED_MEMBER("회원 정보가 삭제되지 않았습니다."),
    INVALID_ARTICLE("유효하지 않은 게시글입니다."),
    NOT_YOUR_COMMENT("댓글에 대한 권한이 없습니다."),
    CATEGORY_REACHED_LIMIT("카테고리 갯수 제한에 도달했습니다."),
    DUPLICATED_EMAIL("이미 가입된 유저입니다.")
    ;
    private String message;
}
