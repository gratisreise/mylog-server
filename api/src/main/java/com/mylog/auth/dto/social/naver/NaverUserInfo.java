package com.mylog.auth.dto.social.naver;

public record NaverUserInfo(
    String resultcode,
    String message,
    NaverResponse response
) { }
