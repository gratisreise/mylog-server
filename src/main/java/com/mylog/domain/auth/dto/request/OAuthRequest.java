package com.mylog.domain.auth.dto.request;

import com.mylog.common.enums.OauthProvider;

public record OAuthRequest(
    OauthProvider provider,
    String code
) {

}
