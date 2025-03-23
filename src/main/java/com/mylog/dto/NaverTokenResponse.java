package com.mylog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class NaverTokenResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("refresh_token")
    private String refreshToken;
    @JsonProperty("token_type")
    private String tokenType;
    @JsonProperty("expires_in")
    private String expiresIn; // 네이버는 문자열로 반환
}
