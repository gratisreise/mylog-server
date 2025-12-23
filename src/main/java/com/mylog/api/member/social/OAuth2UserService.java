package com.mylog.api.member.social;

import com.mylog.model.dto.auth.LoginResponse;
import com.mylog.api.auth.social.OAuth2UserInfo;
import com.mylog.api.auth.social.OAuthRequest;
import com.mylog.domain.entity.Member;

public interface OAuth2UserService {
    LoginResponse login(OAuthRequest oAuthRequest);
    String getAccessToken(OAuthRequest oAuthRequest);
    OAuth2UserInfo getUserInfo(String accessToken);
    Member createOrUpdateMember(OAuth2UserInfo userInfo);
}
