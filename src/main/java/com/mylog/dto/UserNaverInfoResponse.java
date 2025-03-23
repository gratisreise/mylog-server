package com.mylog.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNaverInfoResponse {
    private String resultcode;
    private String message;
    private NaverUserInfo response;
}
