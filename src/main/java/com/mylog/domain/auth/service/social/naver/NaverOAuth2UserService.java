<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/service/social/naver/NaverOAuth2UserService.java
package com.mylog.domain.auth.service.social.naver;


import com.mylog.annotations.OAuth2ServiceType;
import com.mylog.auth.dto.social.OAuth2UserInfo;
import com.mylog.auth.dto.social.OAuthRequest;
import com.mylog.auth.dto.social.naver.NaverOAuth2UserInfo;
import com.mylog.auth.dto.social.naver.NaverTokenResponse;
import com.mylog.auth.dto.social.naver.NaverUserInfo;
import com.mylog.auth.service.RefreshTokenService;
import com.mylog.auth.service.social.AbstractOAuth2UserService;
import com.mylog.auth.service.social.naver.NaverTokenClient;
import com.mylog.auth.service.social.naver.NaverUserClient;
import com.mylog.category.service.CategoryWriter;
import com.mylog.common.annotations.OAuth2ServiceType;
import com.mylog.common.enums.OauthProvider;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.common.security.JwtProvider;
import com.mylog.domain.auth.dto.social.OAuth2UserInfo;
import com.mylog.domain.auth.dto.social.OAuthRequest;
import com.mylog.domain.auth.dto.social.naver.NaverOAuth2UserInfo;
import com.mylog.domain.auth.dto.social.naver.NaverTokenResponse;
import com.mylog.domain.auth.dto.social.naver.NaverUserInfo;
import com.mylog.domain.auth.service.RefreshTokenService;
import com.mylog.domain.auth.service.social.AbstractOAuth2UserService;
import com.mylog.domain.category.service.CategoryWriter;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.repository.MemberRepository;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.common.CMissingDataException;
import com.mylog.exception.common.CommonError;
import com.mylog.member.entity.Member;
import com.mylog.member.repository.MemberRepository;
import com.mylog.member.service.MemberReader;
import com.mylog.member.service.MemberWriter;
import com.mylog.utils.JwtUtil;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@OAuth2ServiceType(OauthProvider.NAVER)
public class NaverOAuth2UserService extends AbstractOAuth2UserService {

    private final MemberWriter memberWriter;
    private final MemberReader memberReader;
    private final NaverTokenClient naverTokenClient;
    private final NaverUserClient naverUserClient;


    @Value("${oauth2.client.naver.client-id}")
    private String clientId;

    @Value("${oauth2.client.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth2.client.naver.redirect-uri}")
    private String redirectUri;

    public NaverOAuth2UserService(
        JwtProvider jwtProvider, RefreshTokenService refreshTokenService,
        CategoryWriter categoryWriter,
        MemberWriter memberWriter,
        MemberReader memberReader,
        NaverTokenClient naverTokenClient, NaverUserClient naverUserClient) {
<<<<<<<< HEAD:src/main/java/com/mylog/domain/auth/service/social/naver/NaverOAuth2UserService.java
        super(jwtProvider, refreshTokenService, categoryWriter);
        this.memberRepository = memberRepository;
========
        super(jwtUtil, refreshTokenService, categoryWriter);
        this.memberWriter = memberWriter;
        this.memberReader = memberReader;
>>>>>>>> origin/main:api/src/main/java/com/mylog/auth/service/social/naver/NaverOAuth2UserService.java
        this.naverTokenClient = naverTokenClient;
        this.naverUserClient = naverUserClient;
    }

    @Override
    public String getAccessToken(OAuthRequest request) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("code", request.getCode());
        params.put("state", request.getState());
        params.put("redirect_uri", redirectUri);

        NaverTokenResponse response = naverTokenClient.getAccessToken(params);

        if (response == null || response.getAccessToken() == null) {
            throw new CMissingDataException(CommonError.TOKEN_IS_EMPTY);
        }

        return response.getAccessToken();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        NaverUserInfo data = naverUserClient.getUserInfo("Bearer " + accessToken);

        if (data == null || data.response() == null) {
            throw new CMissingDataException(CommonError.USER_IS_EMPTY);
        }

        return new NaverOAuth2UserInfo(data);
    }

    @Override
    public Member createOrUpdateMember(OAuth2UserInfo userInfo) {
        Optional<Member> member = memberReader.findByProviderAndProviderId(
            OauthProvider.NAVER,
            userInfo.getId());

        if(member.isEmpty()){
            Member newMember = userInfo.toEntity();
            return memberWriter.saveMember(newMember);
        }

        return member.get();
    }
}
