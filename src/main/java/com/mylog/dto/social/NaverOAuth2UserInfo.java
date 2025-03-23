package com.mylog.dto.social;

import com.mylog.interfaces.OAuth2UserInfo;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;

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
    public String getEmail() {
        return null;
    }

    @Override
    public String getImageUrl() {
        return naverUserInfo.getProfileImage();
    }
}
