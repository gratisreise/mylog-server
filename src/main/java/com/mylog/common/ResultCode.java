package com.mylog.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Getter
public enum ResultCode {
    SUCCESS(200, "success"),
    FAILED(-100, "failed")
    ;
    private int code;
    private String msg;

}
