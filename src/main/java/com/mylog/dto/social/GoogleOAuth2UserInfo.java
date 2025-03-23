package com.mylog.dto.social;

import com.mylog.interfaces.OAuth2UserInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
    private final GoogleUserInfo googleUserInfo;

    @Override
    public String getId() {
        return googleUserInfo.getId();
    }

    @Override
    public String getName() {
        return googleUserInfo.getName();
    }

    @Override
    public String getEmail() {
        return googleUserInfo.getEmail();
    }

    @Override
    public String getImageUrl() {
        return googleUserInfo.getPicture();
    }
}
