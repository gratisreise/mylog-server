package com.mylog.model.dto.auth;

import com.mylog.enums.OauthProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RefreshRequest {
    private String refreshToken;
    private OauthProvider provider;
}
