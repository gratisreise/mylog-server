package com.mylog.model.dto.social;

import com.mylog.enums.OauthProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class OAuthRequest {
    @NotBlank
    private String code;
    @NotBlank
    private OauthProvider provider;
    @NotBlank
    private String state;
}

