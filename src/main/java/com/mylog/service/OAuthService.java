package com.mylog.service;

import com.mylog.config.JwtUtil;
import com.mylog.dto.GoogleTokenResponse;
import com.mylog.dto.GoogleUserInfo;
import com.mylog.dto.LoginResponse;
import com.mylog.dto.NaverTokenResponse;
import com.mylog.dto.NaverUserInfo;
import com.mylog.dto.OAuthRequest;
import com.mylog.dto.UserNaverInfoResponse;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService {
    private final RestTemplate restTemplate;
    private final JwtUtil jwtUtil;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;

    // 구글
    @Value("${oauth2.client.google.client-id}")
    private String googleClientId;
    @Value("${oauth2.client.google.client-secret}")
    private String googleClientSecret;
    @Value("${oauth2.client.google.redirect-uri}")
    private String googleRedirectUri;

    //네이버
    @Value("${oauth2.client.naver.client-id}")
    private String naverClientId;
    @Value("${oauth2.client.naver.client-secret}")
    private String naverClientSecret;
    @Value("${oauth2.client.naver.redirect-uri}")
    private String naverRedirectUri;

    //카카오

    //로그인
    public LoginResponse socialGoogleLogin(OAuthRequest request) {
        //토큰 가져오기
        String accessToken = getToken(request);
        //유저 정보 가져오기
        GoogleUserInfo userInfo = getGoogleUserInfo(accessToken);
        //데이터 멤버로 가공 후 저장
        Member member = getGoogleMember(request, userInfo);
        memberRepository.save(member);

        //리프레쉬 토큰 저장
        String refreshToken = jwtUtil.createRefreshToken(member.getEmail());
        refreshTokenService.saveRefreshToken(member.getEmail(), refreshToken);

        //응답반환
        String jwtAccessToken = jwtUtil.createAccessToken(member.getEmail(), member.getId());
        return new LoginResponse(jwtAccessToken, refreshToken);
    }

    public LoginResponse socialNaverLogin(OAuthRequest request) {
        //토큰 가져오기
        String accessToken = getNaverToken(request);
        //유저 정보 가져오기
        NaverUserInfo userInfo = getNaverUserInfo(accessToken);

        //데이터 멤버로 가공 후 저장
        Member member = getNaverMember(request, userInfo);
        memberRepository.save(member);
        log.info("member: {}", member);

//        리프레쉬 토큰 저장
        String refreshToken = jwtUtil.createRefreshToken(member.getEmail());
        refreshTokenService.saveRefreshToken(member.getEmail(), refreshToken);

        //응답반환
        String jwtAccessToken = jwtUtil.createAccessToken(member.getEmail(), member.getId());
        return new LoginResponse(jwtAccessToken, refreshToken);
    }




    public  NaverUserInfo getNaverUserInfo(String accessToken) {
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
        if(data == null) throw new CMissingDataException("사용자 정보가 비어있습니다.");

        return data.getResponse();
    }

    private GoogleUserInfo getGoogleUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        ResponseEntity<GoogleUserInfo> response = restTemplate.exchange(
            userInfoUrl,
            HttpMethod.GET,
            new HttpEntity<>(headers),
            GoogleUserInfo.class
        );

        if(response.getBody() == null) throw new CMissingDataException("사용자 정보가 비어있습니다.");

        return response.getBody();
    }

    //멤버로 가공
    private Member getGoogleMember(OAuthRequest request, GoogleUserInfo userInfo) {

        //있으면 가져오고 아니면 새로운 멤버 생김
        Member member = memberRepository.findByProviderAndProviderId(
            request.getProvider(),
                userInfo.getId()
            ).orElseGet(Member::new);

        member.setProviderId(userInfo.getId());
        member.setMemberName(userInfo.getName());
        member.setProvider(request.getProvider());
        member.setProfileImg(userInfo.getPicture());

        return member;
    }

    private Member getNaverMember(OAuthRequest request, NaverUserInfo userInfo) {
        //있으면 가져오고 아니면 새로운 멤버 생김
        Member member = memberRepository.findByProviderAndProviderId(
            request.getProvider(),
            userInfo.getId()
        ).orElseGet(Member::new);

        member.setProviderId(userInfo.getId());
        member.setMemberName(userInfo.getName());
        member.setProvider(request.getProvider());
        member.setProfileImg(userInfo.getProfileImage());

        return member;
    }

    //유저정보 접근토큰 가져오기
    private String getToken(OAuthRequest request) {
        log.info("request: {}", request);
        String tokenUrl = "https://oauth2.googleapis.com/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", request.getCode());
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("grant_type", "authorization_code");
        log.info("code {}", request.getCode());
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        try{
            ResponseEntity<GoogleTokenResponse> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                requestEntity,
                GoogleTokenResponse.class
            );

            log.info(response.getBody().toString());

            if(response.getBody() == null){
                log.error("Google OAuth 응답에 토큰이 없습니다.");
                throw new CMissingDataException("토큰 응답이 비어있습니다.");
            }


            return response.getBody().getAccessToken();
        } catch(RestClientException e){
            log.error("구글 토큰 획득에 실패했습니다.", e);
            throw new CMissingDataException("토큰 획득에 실패했습니다.");
        }
    }

    private String getNaverToken(OAuthRequest request) {
        String tokenUrl = "https://nid.naver.com/oauth2.0/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("code", request.getCode());
        params.add("state", request.getState());
        params.add("redirect_uri", naverRedirectUri);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<NaverTokenResponse> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                requestEntity,
                NaverTokenResponse.class
            );

            if (response.getBody() == null) {
                log.error("네이버 OAuth 응답에 토큰이 없습니다.");
                throw new CMissingDataException("토큰 응답이 비어있습니다.");
            }

            return response.getBody().getAccessToken();
        } catch (RestClientException e) {
            log.error("Failed to get token from Naver.", e);
            throw new CMissingDataException("토큰 획득에 실패했습니다.");
        }
    }
}
