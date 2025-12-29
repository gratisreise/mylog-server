package com.mylog.api.auth.dto.social.naver;

public record NaverUserInfo(
    String resultcode,
    String message,
    NaverResponse response
) { }
