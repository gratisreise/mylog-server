package com.mylog.domain.auth.dto.request;

import com.mylog.common.enums.OauthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OAuthRequest(
    @NotNull OauthProvider provider,
    @NotBlank String code
) {

}
