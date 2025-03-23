package com.mylog.dto;

import com.mylog.enums.OauthProvider;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthRequest {
    private String code;
    private OauthProvider provider;
}
