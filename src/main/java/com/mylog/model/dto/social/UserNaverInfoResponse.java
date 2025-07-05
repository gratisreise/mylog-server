package com.mylog.model.dto.social;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNaverInfoResponse {
    private String resultcode;
    private String message;
    private NaverUserInfo response;
}
