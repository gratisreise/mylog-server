package com.mylog.service.social;

import com.mylog.annotations.OAuth2ServiceType;
import com.mylog.config.JwtUtil;
import com.mylog.dto.social.GoogleTokenResponse;
import com.mylog.dto.social.KakaoTokenResponse;
import com.mylog.dto.social.KakaoUserInfo;
import com.mylog.dto.social.KakoOAuth2UserInfo;
import com.mylog.dto.social.OAuthRequest;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.interfaces.OAuth2UserInfo;
import com.mylog.repository.MemberRepository;
import com.mylog.service.RefreshTokenService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@OAuth2ServiceType(OauthProvider.KAKAO)
@Service
@Slf4j
public class KakaoOAuth2UserService extends AbstractOAuth2UserService{

    private final MemberRepository memberRepository;

    @Value("${oauth2.client.kakao.client-id}")
    private String clientId;

    @Value("${oauth2.client.kakao.client-secret}")
    private String clientSecret;

    @Value("${oauth2.client.kakao.redirect-uri}")
    private String redirectUri;

    public KakaoOAuth2UserService(
        RestTemplate restTemplate,
        JwtUtil jwtUtil,
        RefreshTokenService refreshTokenService,
        MemberRepository memberRepository
    ){
        super(restTemplate, jwtUtil, refreshTokenService);
        this.memberRepository = memberRepository;
    }

    @Override
    public String getAccessToken(OAuthRequest request) {
        String tokenUrl = "https://kauth.kakao.com/oauth/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("redirect_uri", redirectUri);
        params.add("code", request.getCode());
        params.add("client_secret", clientSecret);

        log.info("params: {}", params);

        ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            new HttpEntity<>(params, headers),
            KakaoTokenResponse.class
        );

        if(response.getBody() == null){
            throw new CMissingDataException("카카오 토큰이 비어있습니다.");
        }
        log.info("response: {}", response);

        return response.getBody().getAccessToken();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        String userInfoUrl = "https://kapi.kakao.com/v2/user/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<KakaoUserInfo> response = restTemplate.exchange(
            userInfoUrl,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            KakaoUserInfo.class
        );

        if(response.getBody() == null){
            throw new CMissingDataException("카카오 유저정보가 비어있습니다.");
        }

        return new KakoOAuth2UserInfo(response.getBody());
    }

    @Override
    public Member createOrUpdateMember(OAuth2UserInfo userInfo) {
        Member member = memberRepository.findByProviderAndProviderId(
            OauthProvider.KAKAO,
            userInfo.getId()
        ).orElseGet(Member::new);

        member.setProvider(OauthProvider.KAKAO);
        member.setProviderId(userInfo.getId());
        member.setProfileImg(userInfo.getImageUrl());
        member.setNickname(userInfo.getId() + OauthProvider.KAKAO);

        return member;
    }
}
