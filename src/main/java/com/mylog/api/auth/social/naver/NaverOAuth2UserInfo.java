package com.mylog.api.auth.social.naver;

import com.mylog.api.auth.social.OAuth2UserInfo;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NaverOAuth2UserInfo implements OAuth2UserInfo {
    private final NaverUserInfo naverUserInfo;

    @Override
    public String getId() {
        return naverUserInfo.response().id();
    }

    @Override
    public String getName() {
        return naverUserInfo.response().name();
    }

    @Override
    public String getImageUrl() {
        return naverUserInfo.response().profileImage();
    }
}
