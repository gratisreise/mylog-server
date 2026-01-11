package com.mylog.exception.common;


import com.mylog.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum CommonError implements ErrorCode {
    DUPLICATED_EMAIL("COM001", "중복된 데이터가 존재합니다."),
    FAILED_IMAGE_UPLOAD("COM002", "이미지 업로드에 실패했습니다."),
    REFRESH_TOKEN_UNDELETED("COM003", "리프레쉬 토큰이 삭제되지 않았습니다.")

    ;

    private final String code;
    private final String message;
}
