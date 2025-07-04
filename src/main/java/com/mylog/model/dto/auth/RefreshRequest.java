package com.mylog.model.dto.auth;

import com.mylog.enums.OauthProvider;

public record RefreshRequest(String refreshToken, OauthProvider provider) {}
