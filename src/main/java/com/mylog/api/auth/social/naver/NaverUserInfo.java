package com.mylog.api.auth.social.naver;

public record NaverUserInfo(
    String resultcode,
    String message,
    NaverResponse response
) { }
