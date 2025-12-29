package com.mylog.api.auth.dto.social;

import com.mylog.common.enums.OauthProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OAuthRequest {
    @NotBlank
    private String code;
    @NotNull
    private OauthProvider provider;
    @NotBlank
    private String state;
}

