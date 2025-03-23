package com.mylog.interfaces;

import com.mylog.dto.LoginResponse;
import com.mylog.dto.social.OAuthRequest;
import com.mylog.entity.Member;

public interface OAuth2UserService {
    LoginResponse login(OAuthRequest oAuthRequest);
    String getAccessToken(OAuthRequest oAuthRequest);
    OAuth2UserInfo getUserInfo(String accessToken);
    Member createOrUpdateMember(OAuth2UserInfo userInfo);
}
