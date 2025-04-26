package com.mylog.dto.social;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    private final NaverUserInfo naverUserInfo;

    @Override
    public String getId() {
        return naverUserInfo.getId();
    }

    @Override
    public String getName() {
        return naverUserInfo.getName();
    }


    @Override
    public String getImageUrl() {
        return naverUserInfo.getProfileImage();
    }
}
