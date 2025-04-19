package com.mylog.dto.social;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mylog.interfaces.OAuth2UserInfo;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

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
