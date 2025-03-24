package com.mylog.dto;

import com.mylog.enums.OauthProvider;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshRequest {
    private String refreshToken;
    private OauthProvider provider;
}
