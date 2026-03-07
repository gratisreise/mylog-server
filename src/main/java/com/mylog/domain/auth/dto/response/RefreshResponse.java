package com.mylog.domain.auth.dto.response;

public record RefreshResponse (
    String accessToken,
    String refreshToken
){}
