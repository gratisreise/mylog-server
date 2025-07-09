package com.mylog.service.social.naver;


import com.mylog.annotations.OAuth2ServiceType;
import com.mylog.config.JwtUtil;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.social.OAuth2UserInfo;
import com.mylog.model.dto.social.OAuthRequest;
import com.mylog.model.dto.social.naver.NaverOAuth2UserInfo;
import com.mylog.model.dto.social.naver.NaverTokenResponse;
import com.mylog.model.dto.social.naver.NaverUserInfo;
import com.mylog.model.entity.Member;
import com.mylog.repository.MemberRepository;
import com.mylog.service.RefreshTokenService;
import com.mylog.service.social.AbstractOAuth2UserService;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@OAuth2ServiceType(OauthProvider.NAVER)
public class NaverOAuth2UserService extends AbstractOAuth2UserService {
    private final MemberRepository memberRepository;
    private final NaverTokenClient naverTokenClient;
    private final NaverUserClient naverUserClient;


    @Value("${oauth2.client.naver.client-id}")
    private String clientId;

    @Value("${oauth2.client.naver.client-secret}")
    private String clientSecret;

    @Value("${oauth2.client.naver.redirect-uri}")
    private String redirectUri;

    public NaverOAuth2UserService(
        JwtUtil jwtUtil,
        RefreshTokenService refreshTokenService,
        MemberRepository memberRepository,
        NaverTokenClient naverTokenClient, NaverUserClient naverUserClient) {
        super(jwtUtil, refreshTokenService);
        this.memberRepository = memberRepository;
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
            throw new CMissingDataException("토큰 응답이 비어있습니다.");
        }

        return response.getAccessToken();
    }

    @Override
    public OAuth2UserInfo getUserInfo(String accessToken) {
        NaverUserInfo data = naverUserClient.getUserInfo("Bearer " + accessToken);

        if (data == null || data.response() == null) {
            throw new CMissingDataException("사용자 정보가 비어있습니다.");
        }

        return new NaverOAuth2UserInfo(data);
    }

    @Override
    public Member createOrUpdateMember(OAuth2UserInfo userInfo) {
        Member member = memberRepository.findByProviderAndProviderId(
            OauthProvider.NAVER,
            userInfo.getId())
            .orElseGet(Member::new);

        member.update(userInfo, OauthProvider.NAVER);

        return memberRepository.save(member);
    }
}
