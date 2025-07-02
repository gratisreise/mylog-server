package com.mylog.model.dto.social;

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
    public String getImageUrl() {
        return googleUserInfo.getPicture();
    }
}
