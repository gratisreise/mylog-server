package com.mylog.auth.dto;


import com.mylog.enums.OauthProvider;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
    @NotBlank
    String refreshToken,

    @NotBlank
    OauthProvider provider
) {}
