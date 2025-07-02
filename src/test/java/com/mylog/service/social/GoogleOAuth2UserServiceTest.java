package com.mylog.service.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.config.JwtUtil;
import com.mylog.model.dto.social.GoogleOAuth2UserInfo;
import com.mylog.model.dto.social.GoogleTokenResponse;
import com.mylog.model.dto.social.GoogleUserInfo;
import com.mylog.model.dto.social.OAuthRequest;
import com.mylog.model.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import com.mylog.service.RefreshTokenService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2UserServiceTest {

    @InjectMocks
    private GoogleOAuth2UserService googleOAuth2UserService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MemberRepository memberRepository;

    private OAuthRequest oAuthRequest;
    private GoogleTokenResponse tokenResponse;
    private GoogleUserInfo userInfoResponse;
    private GoogleOAuth2UserInfo oAuth2UserInfo;
    private Member member;
    private String accessToken;
    private String userId;
    private String nickname;
    private String clientId;
    private String clientSecret;
    private String redirectUri;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        accessToken = "sampleAccessToken";
        userId = "google123";
        nickname = userId + OauthProvider.GOOGLE;
        clientId = "test-client-id";
        clientSecret = "test-client-secret";
        redirectUri = "http://localhost:8080/oauth2/callback/google";

        // @Value 필드 주입
        ReflectionTestUtils.setField(googleOAuth2UserService, "clientId", clientId);
        ReflectionTestUtils.setField(googleOAuth2UserService, "clientSecret", clientSecret);
        ReflectionTestUtils.setField(googleOAuth2UserService, "redirectUri", redirectUri);

        // OAuthRequest 설정
        oAuthRequest = new OAuthRequest();
        oAuthRequest.setProvider(OauthProvider.GOOGLE);
        oAuthRequest.setCode("authCode");

        // GoogleTokenResponse 설정
        tokenResponse = new GoogleTokenResponse();
        tokenResponse.setAccessToken(accessToken);

        // GoogleUserInfo 및 GoogleOAuth2UserInfo 설정
        userInfoResponse = new GoogleUserInfo();
        userInfoResponse.setId(userId);
        userInfoResponse.setName("Test User");
        userInfoResponse.setPicture("http://example.com/profile.jpg");
        oAuth2UserInfo = new GoogleOAuth2UserInfo(userInfoResponse);

        // Member 설정
        member = Member.builder()
            .providerId(userId)
            .provider(OauthProvider.GOOGLE)
            .memberName("Test User")
            .nickname(nickname)
            .profileImg("http://example.com/profile.jpg")
            .build();
    }

    @Test
    void 액세스_토큰_획득_성공() {
        // Given
        ResponseEntity<GoogleTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        when(restTemplate.exchange(
            eq("https://oauth2.googleapis.com/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(GoogleTokenResponse.class)
        )).thenReturn(responseEntity);

        // When
        String result = googleOAuth2UserService.getAccessToken(oAuthRequest);

        // Then
        assertThat(result).isEqualTo(accessToken);
        verify(restTemplate).exchange(
            eq("https://oauth2.googleapis.com/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(GoogleTokenResponse.class)
        );
    }

    @Test
    void 액세스_토큰_응답_비어있음_예외발생() {
        // Given
        ResponseEntity<GoogleTokenResponse> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.exchange(
            eq("https://oauth2.googleapis.com/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(GoogleTokenResponse.class)
        )).thenReturn(responseEntity);

        // When & Then
        assertThatThrownBy(() -> googleOAuth2UserService.getAccessToken(oAuthRequest))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage("토큰 응답이 비어있습니다.");
        verify(restTemplate).exchange(
            eq("https://oauth2.googleapis.com/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(GoogleTokenResponse.class)
        );
    }

    @Test
    void 사용자_정보_조회_성공() {
        // Given
        ResponseEntity<GoogleUserInfo> responseEntity = ResponseEntity.ok(userInfoResponse);
        when(restTemplate.exchange(
            eq("https://www.googleapis.com/oauth2/v2/userinfo"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(GoogleUserInfo.class)
        )).thenReturn(responseEntity);

        // When
        GoogleOAuth2UserInfo result = (GoogleOAuth2UserInfo) googleOAuth2UserService.getUserInfo(accessToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getImageUrl()).isEqualTo("http://example.com/profile.jpg");
        verify(restTemplate).exchange(
            eq("https://www.googleapis.com/oauth2/v2/userinfo"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(GoogleUserInfo.class)
        );
    }

    @Test
    void 사용자_정보_응답_비어있음_예외발생() {
        // Given
        ResponseEntity<GoogleUserInfo> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.exchange(
            eq("https://www.googleapis.com/oauth2/v2/userinfo"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(GoogleUserInfo.class)
        )).thenReturn(responseEntity);

        // When & Then
        assertThatThrownBy(() -> googleOAuth2UserService.getUserInfo(accessToken))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage("사용자 정보가 비어있습니다.");
        verify(restTemplate).exchange(
            eq("https://www.googleapis.com/oauth2/v2/userinfo"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(GoogleUserInfo.class)
        );
    }

    @Test
    void 신규_멤버_생성_성공() {
        // Given
        when(memberRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, userId))
            .thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        Member result = googleOAuth2UserService.createOrUpdateMember(oAuth2UserInfo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProviderId()).isEqualTo(userId);
        assertThat(result.getProvider()).isEqualTo(OauthProvider.GOOGLE);
        assertThat(result.getMemberName()).isEqualTo("Test User");
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getProfileImg()).isEqualTo("http://example.com/profile.jpg");

        verify(memberRepository).findByProviderAndProviderId(OauthProvider.GOOGLE, userId);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void 기존_멤버_업데이트_성공() {
        // Given
        Member existingMember = Member.builder()
            .providerId(userId)
            .provider(OauthProvider.GOOGLE)
            .memberName("Old Name")
            .nickname("oldNickname")
            .profileImg("http://old.com/profile.jpg")
            .build();
        when(memberRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, userId))
            .thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        Member result = googleOAuth2UserService.createOrUpdateMember(oAuth2UserInfo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProviderId()).isEqualTo(userId);
        assertThat(result.getMemberName()).isEqualTo("Test User");
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getProfileImg()).isEqualTo("http://example.com/profile.jpg");

        verify(memberRepository).findByProviderAndProviderId(OauthProvider.GOOGLE, userId);
        verify(memberRepository).save(any(Member.class));
    }
}