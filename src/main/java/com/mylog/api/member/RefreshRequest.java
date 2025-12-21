package com.mylog.api.member;

import com.mylog.domain.enums.OauthProvider;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank
    String refreshToken,

    @NotBlank
    OauthProvider provider
) {}
