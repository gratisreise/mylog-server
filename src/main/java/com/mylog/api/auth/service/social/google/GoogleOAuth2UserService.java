package com.mylog.api.auth.service.social.google;

import com.mylog.annotations.OAuth2ServiceType;
import com.mylog.api.auth.JwtUtil;
import com.mylog.common.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.api.auth.dto.social.OAuth2UserInfo;
import com.mylog.api.auth.dto.social.OAuthRequest;
import com.mylog.api.auth.dto.social.google.GoogleOAuth2UserInfo;
import com.mylog.api.auth.dto.social.google.GoogleTokenResponse;
import com.mylog.api.auth.dto.social.google.GoogleUserInfo;
import com.mylog.api.member.entity.Member;
import com.mylog.api.member.repository.MemberRepository;
import com.mylog.api.auth.service.RefreshTokenService;
import com.mylog.api.category.service.CategoryWriter;
import com.mylog.api.auth.service.social.AbstractOAuth2UserService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

@Slf4j
@OAuth2ServiceType(OauthProvider.GOOGLE)
public class GoogleOAuth2UserService extends AbstractOAuth2UserService {
    @Value("${oauth2.client.google.client-id}")
    private String clientId;

    @Value("${oauth2.client.google.client-secret}")
    private String clientSecret;

    @Value("${oauth2.client.google.redirect-uri}")
    private String redirectUri;

    private final MemberRepository memberRepository;
    private final GoogleTokenClient googleTokenClient;
    private final GoogleUserClient googleUserClient;

    public GoogleOAuth2UserService(
        JwtUtil jwtUtil, RefreshTokenService refreshTokenService,
        CategoryWriter categoryWriter,
        MemberRepository memberRepository,
        GoogleTokenClient googleTokenClient,
        GoogleUserClient googleUserClient
    ) {
        super(jwtUtil, refreshTokenService, categoryWriter);
        this.memberRepository = memberRepository;
        this.googleTokenClient = googleTokenClient;
        this.googleUserClient = googleUserClient;
    }

    @Override
    public String getAccessToken(OAuthRequest request) {

        Map<String, String> params = new HashMap<>();
        params.put("code", request.getCode());
        params.put("client_id", clientId);
        params.put("client_secret", clientSecret);
        params.put("redirect_uri", redirectUri);
        params.put("grant_type", "authorization_code");

        GoogleTokenResponse response = googleTokenClient.getAccessToken(params);

        if(response == null){
            throw new CMissingDataException("토큰 응답이 비어있습니다.");
        }

        return response.getAccessToken();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        GoogleUserInfo userInfo = googleUserClient.getUserInfo(setBearerAuth(accessToken));
        log.info("googleUserInfo: {}", userInfo);
        if(userInfo == null){
            throw new CMissingDataException("사용자 정보가 비어있습니다.");
        }
        return new GoogleOAuth2UserInfo(userInfo);
    }

    @Override
    public Member createOrUpdateMember(OAuth2UserInfo userInfo) {
        Member member = memberRepository.findByProviderAndProviderId(
            OauthProvider.GOOGLE,
            userInfo.getId()
        ).orElseGet(Member::new);

        member.update(userInfo, OauthProvider.GOOGLE);

        return memberRepository.save(member);
    }
}
