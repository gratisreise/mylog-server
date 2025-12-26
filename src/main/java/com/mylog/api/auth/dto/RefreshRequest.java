package com.mylog.api.auth.dto;

import com.mylog.common.OauthProvider;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank
    String refreshToken,

    @NotBlank
    OauthProvider provider
) {}
