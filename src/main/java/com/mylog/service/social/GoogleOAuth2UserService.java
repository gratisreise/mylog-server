package com.mylog.service.social;

import com.mylog.annotations.OAuth2ServiceType;
import com.mylog.config.JwtUtil;
import com.mylog.model.dto.social.GoogleOAuth2UserInfo;
import com.mylog.model.dto.social.GoogleTokenResponse;
import com.mylog.model.dto.social.GoogleUserInfo;
import com.mylog.model.dto.social.OAuth2UserInfo;
import com.mylog.model.dto.social.OAuthRequest;
import com.mylog.model.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
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

@Service
@OAuth2ServiceType(OauthProvider.GOOGLE)
@Slf4j
public class GoogleOAuth2UserService extends AbstractOAuth2UserService {
    @Value("${oauth2.client.google.client-id}")
    private String clientId;

    @Value("${oauth2.client.google.client-secret}")
    private String clientSecret;

    @Value("${oauth2.client.google.redirect-uri}")
    private String redirectUri;

    private final MemberRepository memberRepository;


    public GoogleOAuth2UserService(
        RestTemplate restTemplate,
        JwtUtil jwtUtil,
        RefreshTokenService refreshTokenService,
        MemberRepository memberRepository
    ) {
        super(restTemplate, jwtUtil, refreshTokenService);
        this.memberRepository = memberRepository;
    }

    @Override
    public String getAccessToken(OAuthRequest oAuthRequest) {
        String tokenUrl = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", oAuthRequest.getCode());
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");
        log.info("params: {}", params);

        ResponseEntity<GoogleTokenResponse> response = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            new HttpEntity<>(params, headers),
            GoogleTokenResponse.class
        );

        if(response.getBody() == null){
            throw new CMissingDataException("토큰 응답이 비어있습니다.");
        }

        return response.getBody().getAccessToken();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
            userInfoUrl,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            GoogleUserInfo.class
        );

        if(response.getBody() == null){
            throw new CMissingDataException("사용자 정보가 비어있습니다.");
        }
        return new GoogleOAuth2UserInfo(response.getBody());
    }

    @Override
    public Member createOrUpdateMember(OAuth2UserInfo userInfo) {
        Member member = memberRepository.findByProviderAndProviderId(
            OauthProvider.GOOGLE,
            userInfo.getId()
        ).orElseGet(Member::new);

        member.setProviderId(userInfo.getId());
        member.setMemberName(userInfo.getName());
        member.setNickname(userInfo.getId() + OauthProvider.GOOGLE);
        member.setProvider(OauthProvider.GOOGLE);
        member.setProfileImg(userInfo.getImageUrl());

        return memberRepository.save(member);
    }
}
