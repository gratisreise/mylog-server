package com.mylog.domain.auth.dto.social.naver;

public record NaverUserInfo(
    String resultcode,
    String message,
    NaverResponse response
) { }
