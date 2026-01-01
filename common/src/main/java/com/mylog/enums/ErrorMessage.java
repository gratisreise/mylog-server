package com.mylog.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ErrorMessage {
    NOT_YOUR_ARICLE("게시글에 대한 권한이 없습니다.");

    private String message;
}
