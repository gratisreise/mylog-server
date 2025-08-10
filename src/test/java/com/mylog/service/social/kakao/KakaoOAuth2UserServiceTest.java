package com.mylog.service.social.kakao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.config.JwtUtil;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.social.OAuth2UserInfo;
import com.mylog.model.dto.social.OAuthRequest;
import com.mylog.model.dto.social.kako.KakaoOAuth2UserInfo;
import com.mylog.model.dto.social.kako.KakaoTokenResponse;
import com.mylog.model.dto.social.kako.KakaoUserInfo;
import com.mylog.model.dto.social.kako.Properties;
import com.mylog.model.entity.Member;
import com.mylog.repository.member.MemberRepository;
import com.mylog.service.RefreshTokenService;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Comprehensive unit tests for KakaoOAuth2UserService
 * Tests OAuth2 flow: token exchange, user info retrieval, member creation/update
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("KakaoOAuth2UserService Unit Tests")
public class KakaoOAuth2UserServiceTest {

    @InjectMocks
    private KakaoOAuth2UserService kakaoOAuth2UserService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private KakaoTokenClient kakaoTokenClient;

    @Mock
    private KakaoUserClient kakaoUserClient;

    private OAuthRequest testOAuthRequest;
    private KakaoTokenResponse testTokenResponse;
    private KakaoUserInfo testKakaoUserInfo;
    private KakaoOAuth2UserInfo testOAuth2UserInfo;
    private Member existingMember;
    private Member newMember;
    private Properties testProperties;

    private static final String TEST_CODE = "test_kakao_auth_code_12345";
    private static final String TEST_ACCESS_TOKEN = "kakao_access_token_example";
    private static final String TEST_REFRESH_TOKEN = "kakao_refresh_token_example";
    private static final String TEST_CLIENT_ID = "test_kakao_client_id";
    private static final String TEST_CLIENT_SECRET = "test_kakao_client_secret";
    private static final String TEST_REDIRECT_URI = "http://localhost:8080/oauth2/callback/kakao";
    private static final Long TEST_USER_ID = 123456789L;
    private static final String TEST_NICKNAME = "testuser";
    private static final String TEST_PROFILE_IMAGE = "http://k.kakaocdn.net/dn/test/profile.jpg";
    private static final Long TEST_MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        // OAuth 설정값 주입
        ReflectionTestUtils.setField(kakaoOAuth2UserService, "clientId", TEST_CLIENT_ID);
        ReflectionTestUtils.setField(kakaoOAuth2UserService, "clientSecret", TEST_CLIENT_SECRET);
        ReflectionTestUtils.setField(kakaoOAuth2UserService, "redirectUri", TEST_REDIRECT_URI);

        // Test OAuthRequest setup
        testOAuthRequest = new OAuthRequest();
        testOAuthRequest.setCode(TEST_CODE);
        testOAuthRequest.setProvider(OauthProvider.KAKAO);

        // Test KakaoTokenResponse setup
        testTokenResponse = new KakaoTokenResponse();
        testTokenResponse.setAccessToken(TEST_ACCESS_TOKEN);
        testTokenResponse.setTokenType("bearer");
        testTokenResponse.setRefreshToken(TEST_REFRESH_TOKEN);
        testTokenResponse.setExpiresIn(21599L);

        // Test Properties setup
        testProperties = new Properties(TEST_PROFILE_IMAGE);

        // Test KakaoUserInfo setup
        testKakaoUserInfo = new KakaoUserInfo(TEST_USER_ID, testProperties);

        // Test KakaoOAuth2UserInfo setup
        testOAuth2UserInfo = new KakaoOAuth2UserInfo(testKakaoUserInfo);

        // Test Member setup - 기존 멤버
        existingMember = Member.builder()
            .id(TEST_MEMBER_ID)
            .nickname(TEST_NICKNAME)
            .memberName("KAKAO" + TEST_USER_ID)
            .profileImg(TEST_PROFILE_IMAGE)
            .provider(OauthProvider.KAKAO)
            .providerId(TEST_USER_ID.toString())
            .build();

        // Test Member setup - 새 멤버
        newMember = new Member();
    }

    @Nested
    @DisplayName("getAccessToken Tests")
    class GetAccessTokenTests {

        @Test
        @DisplayName("OAuth 코드로 액세스 토큰을 정상적으로 가져온다")
        void getAccessToken_WithValidCode_ReturnsAccessToken() {
            // Given
            when(kakaoTokenClient.getAccessToken(any())).thenReturn(testTokenResponse);

            // When
            String accessToken = kakaoOAuth2UserService.getAccessToken(testOAuthRequest);

            // Then
            assertThat(accessToken).isEqualTo(TEST_ACCESS_TOKEN);

            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(kakaoTokenClient).getAccessToken(paramsCaptor.capture());
            
            Map<String, String> capturedParams = paramsCaptor.getValue();
            assertThat(capturedParams).containsEntry("code", TEST_CODE);
            assertThat(capturedParams).containsEntry("client_id", TEST_CLIENT_ID);
            assertThat(capturedParams).containsEntry("client_secret", TEST_CLIENT_SECRET);
            assertThat(capturedParams).containsEntry("redirect_uri", TEST_REDIRECT_URI);
            assertThat(capturedParams).containsEntry("grant_type", "authorization_code");
        }

        @Test
        @DisplayName("토큰 응답이 null인 경우 예외를 발생시킨다")
        void getAccessToken_WhenTokenResponseIsNull_ThrowsException() {
            // Given
            when(kakaoTokenClient.getAccessToken(any())).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> kakaoOAuth2UserService.getAccessToken(testOAuthRequest))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("토큰 응답이 비어있습니다.");
        }

        @Test
        @DisplayName("액세스 토큰이 null인 경우 예외를 발생시킨다")
        void getAccessToken_WhenAccessTokenIsNull_ThrowsException() {
            // Given
            KakaoTokenResponse nullTokenResponse = new KakaoTokenResponse();
            nullTokenResponse.setAccessToken(null);
            when(kakaoTokenClient.getAccessToken(any())).thenReturn(nullTokenResponse);

            // When & Then
            assertThatThrownBy(() -> kakaoOAuth2UserService.getAccessToken(testOAuthRequest))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("토큰 응답이 비어있습니다.");
        }


        @Test
        @DisplayName("매우 긴 코드로도 토큰 요청을 처리한다")
        void getAccessToken_WithVeryLongCode_ProcessesNormally() {
            // Given
            String longCode = "a".repeat(1000);
            OAuthRequest longCodeRequest = new OAuthRequest();
            longCodeRequest.setCode(longCode);
            longCodeRequest.setProvider(OauthProvider.KAKAO);
            when(kakaoTokenClient.getAccessToken(any())).thenReturn(testTokenResponse);

            // When
            String accessToken = kakaoOAuth2UserService.getAccessToken(longCodeRequest);

            // Then
            assertThat(accessToken).isEqualTo(TEST_ACCESS_TOKEN);
            
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(kakaoTokenClient).getAccessToken(paramsCaptor.capture());
            
            Map<String, String> capturedParams = paramsCaptor.getValue();
            assertThat(capturedParams).containsEntry("code", longCode);
        }
    }

    @Nested
    @DisplayName("getUserInfo Tests")
    class GetUserInfoTests {

        @Test
        @DisplayName("액세스 토큰으로 사용자 정보를 정상적으로 가져온다")
        void getUserInfo_WithValidAccessToken_ReturnsUserInfo() {
            // Given
            when(kakaoUserClient.getUserInfo("Bearer " + TEST_ACCESS_TOKEN))
                .thenReturn(testKakaoUserInfo);

            // When
            OAuth2UserInfo userInfo = kakaoOAuth2UserService.getUserInfo(TEST_ACCESS_TOKEN);

            // Then
            assertThat(userInfo).isInstanceOf(KakaoOAuth2UserInfo.class);
            assertThat(userInfo.getId()).isEqualTo(TEST_USER_ID.toString());
            assertThat(userInfo.getName()).isEqualTo("KAKAO" + TEST_USER_ID);
            assertThat(userInfo.getImageUrl()).isEqualTo(TEST_PROFILE_IMAGE);

            verify(kakaoUserClient).getUserInfo("Bearer " + TEST_ACCESS_TOKEN);
        }

        @Test
        @DisplayName("사용자 정보 응답이 null인 경우 예외를 발생시킨다")
        void getUserInfo_WhenUserInfoIsNull_ThrowsException() {
            // Given
            when(kakaoUserClient.getUserInfo(anyString())).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> kakaoOAuth2UserService.getUserInfo(TEST_ACCESS_TOKEN))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("카카오 유저정보가 비어있습니다.");
        }

        @Test
        @DisplayName("Bearer 토큰 형식이 올바르게 설정된다")
        void getUserInfo_SetsBearerTokenFormatCorrectly() {
            // Given
            String customAccessToken = "custom_kakao_token_123";
            when(kakaoUserClient.getUserInfo("Bearer " + customAccessToken))
                .thenReturn(testKakaoUserInfo);

            // When
            kakaoOAuth2UserService.getUserInfo(customAccessToken);

            // Then
            verify(kakaoUserClient).getUserInfo("Bearer " + customAccessToken);
        }

    }

    @Nested
    @DisplayName("createOrUpdateMember Tests")
    class CreateOrUpdateMemberTests {

        @Test
        @DisplayName("기존 멤버가 있는 경우 업데이트한다")
        void createOrUpdateMember_WhenMemberExists_UpdatesMember() {
            // Given
            when(memberRepository.findByProviderAndProviderId(OauthProvider.KAKAO, TEST_USER_ID.toString()))
                .thenReturn(Optional.of(existingMember));
            when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

            // When
            Member result = kakaoOAuth2UserService.createOrUpdateMember(testOAuth2UserInfo);

            // Then
            assertThat(result).isEqualTo(existingMember);
            verify(memberRepository).findByProviderAndProviderId(OauthProvider.KAKAO, TEST_USER_ID.toString());
            verify(memberRepository).save(existingMember);
        }

        @Test
        @DisplayName("새 멤버인 경우 새로 생성한다")
        void createOrUpdateMember_WhenMemberNotExists_CreatesNewMember() {
            // Given
            when(memberRepository.findByProviderAndProviderId(OauthProvider.KAKAO, TEST_USER_ID.toString()))
                .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(newMember);

            // When
            Member result = kakaoOAuth2UserService.createOrUpdateMember(testOAuth2UserInfo);

            // Then
            assertThat(result).isEqualTo(newMember);
            verify(memberRepository).findByProviderAndProviderId(OauthProvider.KAKAO, TEST_USER_ID.toString());
            
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository).save(memberCaptor.capture());
            
            Member capturedMember = memberCaptor.getValue();
            assertThat(capturedMember).isNotNull();
        }


        @Test
        @DisplayName("매우 큰 ID를 가진 사용자도 처리한다")
        void createOrUpdateMember_WithVeryLargeId_ProcessesNormally() {
            // Given
            Long largeId = Long.MAX_VALUE;
            Properties largeIdProperties = new Properties(TEST_PROFILE_IMAGE);
            KakaoUserInfo largeIdUserInfo = new KakaoUserInfo(largeId, largeIdProperties);
            KakaoOAuth2UserInfo largeIdOAuth2UserInfo = new KakaoOAuth2UserInfo(largeIdUserInfo);

            when(memberRepository.findByProviderAndProviderId(OauthProvider.KAKAO, largeId.toString()))
                .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(newMember);

            // When
            Member result = kakaoOAuth2UserService.createOrUpdateMember(largeIdOAuth2UserInfo);

            // Then
            assertThat(result).isEqualTo(newMember);
            verify(memberRepository).findByProviderAndProviderId(OauthProvider.KAKAO, largeId.toString());
        }
    }

    @Nested
    @DisplayName("setBearerAuth method Tests")
    class SetBearerAuthTests {

        @Test
        @DisplayName("액세스 토큰에 Bearer 접두사를 올바르게 추가한다")
        void setBearerAuth_AddsCorrectPrefix() {
            // Given - getUserInfo 메소드를 통해 setBearerAuth 테스트
            when(kakaoUserClient.getUserInfo("Bearer " + TEST_ACCESS_TOKEN))
                .thenReturn(testKakaoUserInfo);

            // When
            OAuth2UserInfo userInfo = kakaoOAuth2UserService.getUserInfo(TEST_ACCESS_TOKEN);

            // Then
            assertThat(userInfo).isNotNull();
            verify(kakaoUserClient).getUserInfo("Bearer " + TEST_ACCESS_TOKEN);
        }

    }

}