package com.mylog.domain.auth.service.social.kakao;

import com.mylog.common.annotations.OAuth2ServiceType;
import com.mylog.common.security.JwtUtil;
import com.mylog.common.enums.OauthProvider;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.domain.auth.dto.social.OAuth2UserInfo;
import com.mylog.domain.auth.dto.social.OAuthRequest;
import com.mylog.domain.auth.dto.social.kako.KakaoOAuth2UserInfo;
import com.mylog.domain.auth.dto.social.kako.KakaoTokenResponse;
import com.mylog.domain.auth.dto.social.kako.KakaoUserInfo;
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
@OAuth2ServiceType(OauthProvider.KAKAO)
public class KakaoOAuth2UserService extends AbstractOAuth2UserService {

    private final MemberRepository memberRepository;
    private final KakaoTokenClient kakaoTokenClient;
    private final KakaoUserClient kakaoUserClient;

    @Value("${oauth2.client.kakao.client-id}")
    private String clientId;

    @Value("${oauth2.client.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth2.client.kakao.redirect-uri}")
    private String redirectUri;

    public KakaoOAuth2UserService(
        JwtUtil jwtUtil, RefreshTokenService refreshTokenService,
        CategoryWriter categoryWriter,
        MemberRepository memberRepository,
        KakaoTokenClient kakaoTokenClient,
        KakaoUserClient kakaoUserClient
    ){
        super(jwtUtil, refreshTokenService, categoryWriter);
        this.memberRepository = memberRepository;
        this.kakaoTokenClient = kakaoTokenClient;
        this.kakaoUserClient = kakaoUserClient;
    }

    @Override
    public String getAccessToken(OAuthRequest request) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "authorization_code");
        params.put("client_id", clientId);
        params.put("redirect_uri", redirectUri);
        params.put("code", request.getCode());
        params.put("client_secret", clientSecret);

        KakaoTokenResponse response = kakaoTokenClient.getAccessToken(params);

        if (response == null || response.getAccessToken() == null) {
            throw new CMissingDataException("토큰 응답이 비어있습니다.");
        }

        return response.getAccessToken();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        KakaoUserInfo userInfo = kakaoUserClient.getUserInfo(setBearerAuth(accessToken));

        if (userInfo == null) {
            throw new CMissingDataException("카카오 유저정보가 비어있습니다.");
        }

        return new KakaoOAuth2UserInfo(userInfo);
    }

    @Override
    public Member createOrUpdateMember(OAuth2UserInfo userInfo) {
        Member member = memberRepository.findByProviderAndProviderId(
            OauthProvider.KAKAO,
            userInfo.getId()
        ).orElseGet(Member::new);

        member.update(userInfo, OauthProvider.KAKAO);

        return memberRepository.save(member);
    }
}
