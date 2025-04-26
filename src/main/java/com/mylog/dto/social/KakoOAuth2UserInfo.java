package com.mylog.dto.social;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class KakoOAuth2UserInfo implements OAuth2UserInfo {
    private final KakaoUserInfo kakaoUserInfo;

    @Override
    public String getId() {
        return kakaoUserInfo.getId().toString();
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return kakaoUserInfo.getProperties().getProfileImage();
    }
}
