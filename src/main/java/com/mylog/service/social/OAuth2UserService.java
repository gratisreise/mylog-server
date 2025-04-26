package com.mylog.service.social;

import com.mylog.dto.LoginResponse;
import com.mylog.dto.social.OAuthRequest;
import com.mylog.entity.Member;
import com.mylog.dto.social.OAuth2UserInfo;

public interface OAuth2UserService {
    LoginResponse login(OAuthRequest oAuthRequest);
    String getAccessToken(OAuthRequest oAuthRequest);
    OAuth2UserInfo getUserInfo(String accessToken);
    Member createOrUpdateMember(OAuth2UserInfo userInfo);
}
