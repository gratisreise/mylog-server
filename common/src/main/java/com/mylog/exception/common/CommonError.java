package com.mylog.exception.common;


import com.mylog.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonError implements ErrorCode {
    DUPLICATED_EMAIL("COM001", "중복된 데이터가 존재합니다."),
    FAILED_IMAGE_UPLOAD("COM002", "이미지 업로드에 실패했습니다."),
    REFRESH_TOKEN_UNDELETED("COM003", "리프레쉬 토큰이 삭제되지 않았습니다."),
    INVALID_TOKEN("COM004", "유효하지 않은 토큰입니다."),
    TOKEN_IS_EMPTY("COM000", "토큰 응답이 비어있습니다."),
    USER_IS_EMPTY("COM000", "사용자 정보가 비어있습니다."),
    MEMBER_IS_EMPTY("COM000", "회원정보 조회에 실패했습니다."),
    FAILED_DELETE_MEMBER("COM004", "회원 정보 삭제에 실패했습니다."),
    ARTICLE_IS_EMPTY("COM00", "존재하지 않는 게시글입니다."),
    COMMENT_IS_EMPTY("COM000", "존재하지 않는 댓글입니다."),
    NOT_YOUR_COMMENT("COM005", "댓글에 대한 권한이 없습니다."),
    NOT_YOUR_CATEGORY("COM005", "카테고리에 대한 권한이 없습니다.")
    ;

    private final String code;
    private final String message;
}
