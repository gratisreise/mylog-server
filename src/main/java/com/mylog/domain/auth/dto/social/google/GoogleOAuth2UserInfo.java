package com.mylog.domain.auth.dto.social.google;

import com.mylog.domain.auth.dto.social.OAuth2UserInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    private final GoogleUserInfo googleUserInfo;

    @Override
    public String getId() {
        return googleUserInfo.id();
    }

    @Override
    public String getName() {
        return googleUserInfo.name();
    }


    @Override
    public String getImageUrl() {
        return googleUserInfo.picture();
    }
}
