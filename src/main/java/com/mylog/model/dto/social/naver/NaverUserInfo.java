package com.mylog.model.dto.social.naver;

public record NaverUserInfo(
    String resultcode,
    String message,
    NaverResponse response
) { }
