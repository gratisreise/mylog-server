package com.mylog.api.auth;

public record LoginResponse (String accessToken, String refreshToken) {}
