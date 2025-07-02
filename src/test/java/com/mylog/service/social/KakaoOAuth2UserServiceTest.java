package com.mylog.service.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.config.JwtUtil;
import com.mylog.model.dto.social.KakaoOAuth2UserInfo;
import com.mylog.model.dto.social.KakaoProperties;
import com.mylog.model.dto.social.KakaoTokenResponse;
import com.mylog.model.dto.social.KakaoUserInfo;
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
class KakaoOAuth2UserServiceTest {

    @InjectMocks
    private KakaoOAuth2UserService kakaoOAuth2UserService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MemberRepository memberRepository;

    private OAuthRequest oAuthRequest;
    private KakaoTokenResponse tokenResponse;
    private KakaoUserInfo userInfoResponse;
    private KakaoOAuth2UserInfo oAuth2UserInfo;
    private Member member;
    private String accessToken;
    private Long userId;
    private String nickname;
    private String clientId;
    private String clientSecret;
    private String redirectUri;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        accessToken = "sampleAccessToken";
        userId = 1L;
        nickname = "1" + OauthProvider.KAKAO.name();
        clientId = "test-client-id";
        clientSecret = "test-client-secret";
        redirectUri = "http://localhost:8080/oauth2/callback/kakao";

        // @Value 필드 주입
        ReflectionTestUtils.setField(kakaoOAuth2UserService, "clientId", clientId);
        ReflectionTestUtils.setField(kakaoOAuth2UserService, "clientSecret", clientSecret);
        ReflectionTestUtils.setField(kakaoOAuth2UserService, "redirectUri", redirectUri);

        // OAuthRequest 설정
        oAuthRequest = new OAuthRequest();
        oAuthRequest.setProvider(OauthProvider.KAKAO);
        oAuthRequest.setCode("authCode");

        // KakaoTokenResponse 설정
        tokenResponse = new KakaoTokenResponse();
        tokenResponse.setAccessToken(accessToken);

        // KakaoProperties설정
        KakaoProperties properties = new KakaoProperties("http://example.com/profile.jpg");

        // KakaoUserInfo 및 KakaoOAuth2UserInfo 설정
        userInfoResponse = new KakaoUserInfo();
        userInfoResponse.setId(userId);
        userInfoResponse.setProperties(properties);
        oAuth2UserInfo = new KakaoOAuth2UserInfo(userInfoResponse);

        // Member 설정
        member = Member.builder()
            .providerId(oAuth2UserInfo.getId())
            .provider(OauthProvider.KAKAO)
            .nickname(nickname)
            .profileImg("http://example.com/profile.jpg")
            .build();
    }

    @Test
    void 액세스_토큰_획득_성공() {
        // Given
        ResponseEntity<KakaoTokenResponse> responseEntity = ResponseEntity.ok(tokenResponse);
        when(restTemplate.exchange(
            eq("https://kauth.kakao.com/oauth/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(KakaoTokenResponse.class)
        )).thenReturn(responseEntity);

        // When
        String result = kakaoOAuth2UserService.getAccessToken(oAuthRequest);

        // Then
        assertThat(result).isEqualTo(accessToken);
        verify(restTemplate).exchange(
            eq("https://kauth.kakao.com/oauth/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(KakaoTokenResponse.class)
        );
    }

    @Test
    void 액세스_토큰_응답_비어있음_예외발생() {
        // Given
        ResponseEntity<KakaoTokenResponse> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.exchange(
            eq("https://kauth.kakao.com/oauth/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(KakaoTokenResponse.class)
        )).thenReturn(responseEntity);

        // When & Then
        assertThatThrownBy(() -> kakaoOAuth2UserService.getAccessToken(oAuthRequest))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage("카카오 토큰이 비어있습니다.");
        verify(restTemplate).exchange(
            eq("https://kauth.kakao.com/oauth/token"),
            eq(HttpMethod.POST),
            any(HttpEntity.class),
            eq(KakaoTokenResponse.class)
        );
    }

    @Test
    void 사용자_정보_조회_성공() {
        // Given
        ResponseEntity<KakaoUserInfo> responseEntity = ResponseEntity.ok(userInfoResponse);
        when(restTemplate.exchange(
            eq("https://kapi.kakao.com/v2/user/me"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(KakaoUserInfo.class)
        )).thenReturn(responseEntity);

        // When
        KakaoOAuth2UserInfo result = (KakaoOAuth2UserInfo) kakaoOAuth2UserService.getUserInfo(accessToken);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(oAuth2UserInfo.getId());
        assertThat(result.getImageUrl()).isEqualTo("http://example.com/profile.jpg");
        verify(restTemplate).exchange(
            eq("https://kapi.kakao.com/v2/user/me"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(KakaoUserInfo.class)
        );
    }

    @Test
    void 사용자_정보_응답_비어있음_예외발생() {
        // Given
        ResponseEntity<KakaoUserInfo> responseEntity = ResponseEntity.ok(null);
        when(restTemplate.exchange(
            eq("https://kapi.kakao.com/v2/user/me"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(KakaoUserInfo.class)
        )).thenReturn(responseEntity);

        // When & Then
        assertThatThrownBy(() -> kakaoOAuth2UserService.getUserInfo(accessToken))
            .isInstanceOf(CMissingDataException.class)
            .hasMessage("카카오 유저정보가 비어있습니다.");
        verify(restTemplate).exchange(
            eq("https://kapi.kakao.com/v2/user/me"),
            eq(HttpMethod.GET),
            any(HttpEntity.class),
            eq(KakaoUserInfo.class)
        );
    }

    @Test
    void 신규_멤버_생성_성공() {
        // Given
        when(memberRepository.findByProviderAndProviderId(OauthProvider.KAKAO, oAuth2UserInfo.getId()))
            .thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        // When
        Member result = kakaoOAuth2UserService.createOrUpdateMember(oAuth2UserInfo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProviderId()).isEqualTo(oAuth2UserInfo.getId());
        assertThat(result.getProvider()).isEqualTo(OauthProvider.KAKAO);
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getProfileImg()).isEqualTo("http://example.com/profile.jpg");

        verify(memberRepository).findByProviderAndProviderId(OauthProvider.KAKAO, oAuth2UserInfo.getId());
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void 기존_멤버_업데이트_성공() {
        // Given
        Member existingMember = Member.builder()
            .providerId("oldId")
            .provider(OauthProvider.KAKAO)
            .nickname("oldNickname")
            .profileImg("http://old.com/profile.jpg")
            .build();
        when(memberRepository.findByProviderAndProviderId(OauthProvider.KAKAO, oAuth2UserInfo.getId()))
            .thenReturn(Optional.of(existingMember));
        when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

        // When
        Member result = kakaoOAuth2UserService.createOrUpdateMember(oAuth2UserInfo);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getProviderId()).isEqualTo(oAuth2UserInfo.getId());
        assertThat(result.getProvider()).isEqualTo(OauthProvider.KAKAO);
        assertThat(result.getNickname()).isEqualTo(nickname);
        assertThat(result.getProfileImg()).isEqualTo("http://example.com/profile.jpg");

        verify(memberRepository).findByProviderAndProviderId(OauthProvider.KAKAO, oAuth2UserInfo.getId());
        verify(memberRepository).save(any(Member.class));
    }
}