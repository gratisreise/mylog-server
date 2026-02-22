package com.mylog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResultCode {

    //성공
    SUCCESS("1", "success"),



    //실패
    FAILED("-1", "failed"),

//    DATA_MISSED(-101, "정보가 없습니다."),
//    DATA_DUPLICATED(-102, "중복된 정보입니다."),
//    DATA_INVALID(-103, "유효하지 않은 정보입니다."),
//    UNAUTHORIZED_ACCESS(-104, "허용되지 않는 접근입니다."),
//    UNKNOWN_ERROR(-1, "정의되지 않은 에러입니다.")
    ;




    private String code;
    private String msg;

}
