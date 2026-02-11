package com.mylog.domain.auth.service.social.google;

import com.mylog.common.annotations.OAuth2ServiceType;
import com.mylog.common.security.JwtUtil;
import com.mylog.common.enums.OauthProvider;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.domain.auth.dto.social.OAuth2UserInfo;
import com.mylog.domain.auth.dto.social.OAuthRequest;
import com.mylog.domain.auth.dto.social.google.GoogleOAuth2UserInfo;
import com.mylog.domain.auth.dto.social.google.GoogleTokenResponse;
import com.mylog.domain.auth.dto.social.google.GoogleUserInfo;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.repository.MemberRepository;
import com.mylog.domain.auth.service.RefreshTokenService;
import com.mylog.domain.category.service.CategoryWriter;
import com.mylog.domain.auth.service.social.AbstractOAuth2UserService;
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
