package com.mylog.dto;

import com.mylog.enums.OauthProvider;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OAuthRequest {
    private String code;
    private OauthProvider provider;
    private String state;
}

