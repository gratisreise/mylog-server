package com.mylog.service.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.config.JwtUtil;
import com.mylog.model.dto.social.naver.UserNaverInfoResponse;
import com.mylog.model.dto.social.naver.NaverOAuth2UserInfo;
import com.mylog.model.dto.social.naver.NaverTokenResponse;
import com.mylog.model.dto.social.naver.NaverUserInfo;
import com.mylog.model.dto.social.OAuthRequest;
import com.mylog.model.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import com.mylog.service.RefreshTokenService;
import com.mylog.service.social.naver.NaverOAuth2UserService;
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
class NaverOAuth2UserServiceTest {

    @InjectMocks
    private NaverOAuth2UserService naverOAuth2UserService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MemberRepository memberRepository;

    private OAuthRequest oAuthRequest;
    private NaverTokenResponse tokenResponse;
    private UserNaverInfoResponse userInfoResponse;
    private NaverOAuth2UserInfo oAuth2UserInfo;
    private Member member;
    private String accessToken;
    private String userId;
    private String nickname;
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String resultcode;
    private String message;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        accessToken = "sampleAccessToken";
        userId = "naver123";
        nickname = userId + OauthProvider.NAVER;
        clientId = "test-client-id";
        clientSecret = "test-client-secret";
        redirectUri = "http://localhost:8080/oauth2/callback/naver";
        resultcode = "00";
        message = "success";

        // @Value 필드 주입
        ReflectionTestUtils.setField(naverOAuth2UserService, "clientId", clientId);
        ReflectionTestUtils.setField(naverOAuth2UserService, "clientSecret", clientSecret);
        ReflectionTestUtils.setField(naverOAuth2UserService, "redirectUri", redirectUri);

        // OAuthRequest 설정
        oAuthRequest = new OAuthRequest();
        oAuthRequest.setProvider(OauthProvider.NAVER);
        oAuthRequest.setCode("authCode");
        oAuthRequest.setState("testState");

        // NaverTokenResponse 설정
        tokenResponse = new NaverTokenResponse();
        tokenResponse.setAccessToken(accessToken);

        // UserNaverInfoResponse 및 NaverOAuth2UserInfo 설정
        NaverUserInfo naverUserInfo = new NaverUserInfo();
        naverUserInfo.setId(userId);
        naverUserInfo.setName("Test User");
        naverUserInfo.setProfileImage("http://example.com/profile.jpg");
        userInfoResponse = new UserNaverInfoResponse();
        userInfoResponse.setUserInfo(naverUserInfo);
        oAuth2UserInfo = new NaverOAuth2UserInfo(naverUserInfo);


        // Member 설정
        member = Member.builder()
            .providerId(userId)
            .provider(OauthProvider.NAVER)
            .memberName("Test User")
            .nickname(nickname)
            .profileImg("http://example.com/profile.jpg")
            .build();
    }

    @Test
    void 액세스_토큰_획득_성공() {
        // Given
        ResponseEntity<NaverTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        when(restTemplate.exchange(
            eq("https://nid.naver.com/oauth2.0/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverTokenResponse.class)
        )).thenReturn(responseEntity);

        // When
        String result = naverOAuth2UserService.getAccessToken(oAuthRequest);

        // Then
        assertThat(result).isEqualTo(accessToken);
        verify(restTemplate).exchange(
            eq("https://nid.naver.com/oauth2.0/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverTokenResponse.class)
        );
    }

    @Test
    void 액세스_토큰_응답_비어있음_예외발생() {
        // Given
        ResponseEntity<NaverTokenResponse> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.exchange(
            eq("https://nid.naver.com/oauth2.0/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverTokenResponse.class)
        )).thenReturn(responseEntity);

        // When & Then
        assertThatThrownBy(() -> naverOAuth2UserService.getAccessToken(oAuthRequest))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage("토큰 응답이 비어있습니다.");
        verify(restTemplate).exchange(
            eq("https://nid.naver.com/oauth2.0/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(NaverTokenResponse.class)
        );
    }

    @Test
    void 사용자_정보_조회_성공() {
        // Given
        ResponseEntity<UserNaverInfoResponse> responseEntity = ResponseEntity.ok(userInfoResponse);
        when(restTemplate.exchange(
            eq("https://openapi.naver.com/v1/nid/me"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(UserNaverInfoResponse.class)
        )).thenReturn(responseEntity);

        // When
        NaverOAuth2UserInfo result = (NaverOAuth2UserInfo) naverOAuth2UserService.getUserInfo(accessToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getName()).isEqualTo("Test User");
        assertThat(result.getImageUrl()).isEqualTo("http://example.com/profile.jpg");
        verify(restTemplate).exchange(
            eq("https://openapi.naver.com/v1/nid/me"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(UserNaverInfoResponse.class)
        );
    }

    @Test
    void 사용자_정보_응답_비어있음_예외발생() {
        // Given
        ResponseEntity<UserNaverInfoResponse> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.exchange(
            eq("https://openapi.naver.com/v1/nid/me"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(UserNaverInfoResponse.class)
        )).thenReturn(responseEntity);

        // When & Then
        assertThatThrownBy(() -> naverOAuth2UserService.getUserInfo(accessToken))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage("사용자 정보가 비어있습니다.");
        verify(restTemplate).exchange(
            eq("https://openapi.naver.com/v1/nid/me"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(UserNaverInfoResponse.class)
        );
    }

    @Test
    void 신규_멤버_생성_성공() {
        // Given
        when(memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, userId))
            .thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        Member result = naverOAuth2UserService.createOrUpdateMember(oAuth2UserInfo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProviderId()).isEqualTo(userId);
        assertThat(result.getProvider()).isEqualTo(OauthProvider.NAVER);
        assertThat(result.getMemberName()).isEqualTo("Test User");
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getProfileImg()).isEqualTo("http://example.com/profile.jpg");

        verify(memberRepository).findByProviderAndProviderId(OauthProvider.NAVER, userId);
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void 기존_멤버_업데이트_성공() {
        // Given
        Member existingMember = Member.builder()
            .providerId(userId)
            .provider(OauthProvider.NAVER)
            .memberName("Old Name")
            .nickname("oldNickname")
            .profileImg("http://old.com/profile.jpg")
            .build();
        when(memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, userId))
            .thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        Member result = naverOAuth2UserService.createOrUpdateMember(oAuth2UserInfo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProviderId()).isEqualTo(userId);
        assertThat(result.getProvider()).isEqualTo(OauthProvider.NAVER);
        assertThat(result.getMemberName()).isEqualTo("Test User");
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getProfileImg()).isEqualTo("http://example.com/profile.jpg");

        verify(memberRepository).findByProviderAndProviderId(OauthProvider.NAVER, userId);
        verify(memberRepository).save(any(Member.class));
    }
}