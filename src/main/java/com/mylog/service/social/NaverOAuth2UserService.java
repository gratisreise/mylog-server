package com.mylog.service.social;


import com.mylog.annotations.OAuth2ServiceType;
import com.mylog.config.JwtUtil;
import com.mylog.dto.member.UserNaverInfoResponse;
import com.mylog.dto.social.NaverOAuth2UserInfo;
import com.mylog.dto.social.NaverTokenResponse;
import com.mylog.dto.social.OAuth2UserInfo;
import com.mylog.dto.social.OAuthRequest;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import com.mylog.service.RefreshTokenService;
import java.util.Collections;
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
@Slf4j
@OAuth2ServiceType(OauthProvider.NAVER)
public class NaverOAuth2UserService extends AbstractOAuth2UserService {
    private final MemberRepository memberRepository;

    @Value("${oauth2.client.naver.client-id}")
    private String clientId;

    @Value("${oauth2.client.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth2.client.naver.redirect-uri}")
    private String redirectUri;


    public NaverOAuth2UserService(
        RestTemplate restTemplate,
        JwtUtil jwtUtil,
        RefreshTokenService refreshTokenService,
        MemberRepository memberRepository
    ) {
        super(restTemplate, jwtUtil, refreshTokenService);
        this.memberRepository = memberRepository;
    }

    @Override
    public String getAccessToken(OAuthRequest request) {
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        log.info("request: {}", request);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", request.getCode());
        params.add("state", request.getState());
        params.add("redirect_uri", redirectUri);
        log.info("params: {}", params);

        ResponseEntity<NaverTokenResponse> response = restTemplate.exchange(
            tokenUrl,
            HttpMethod.POST,
            new HttpEntity<>(params, headers),
            NaverTokenResponse.class
        );

        log.info("response: {}", response);

        if(response.getBody() == null){
            throw new CMissingDataException("토큰 응답이 비어있습니다.");
        }

        return response.getBody().getAccessToken();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        String userInfoUrl = "https://openapi.naver.com/v1/nid/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<UserNaverInfoResponse> response = restTemplate.exchange(
            userInfoUrl,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            UserNaverInfoResponse.class
        );

        UserNaverInfoResponse data = response.getBody();
        if(data == null){
            throw new CMissingDataException("사용자 정보가 비어있습니다.");
        }
        return new NaverOAuth2UserInfo(data.getResponse());
    }

    @Override
    public Member createOrUpdateMember(OAuth2UserInfo userInfo) {
        Member member = memberRepository.findByProviderAndProviderId(
            OauthProvider.NAVER,
            userInfo.getId())
            .orElseGet(Member::new);

        member.setProviderId(userInfo.getId());
        member.setMemberName(userInfo.getName());
        member.setProvider(OauthProvider.NAVER);
        member.setProfileImg(userInfo.getImageUrl());
        member.setNickname(userInfo.getId() + OauthProvider.NAVER);

        return memberRepository.save(member);
    }
}
