package com.mylog.service.social.google;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.config.JwtUtil;
import com.mylog.domain.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.social.OAuth2UserInfo;
import com.mylog.model.dto.social.OAuthRequest;
import com.mylog.model.dto.social.google.GoogleOAuth2UserInfo;
import com.mylog.model.dto.social.google.GoogleTokenResponse;
import com.mylog.model.dto.social.google.GoogleUserInfo;
import com.mylog.domain.entity.Member;
import com.mylog.api.member.MemberRepository;
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
 * Comprehensive unit tests for GoogleOAuth2UserService
 * Tests OAuth2 flow: token exchange, user info retrieval, member creation/update
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GoogleOAuth2UserService Unit Tests")
public class GoogleOAuth2UserServiceTest {

    @InjectMocks
    private GoogleOAuth2UserService googleOAuth2UserService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GoogleTokenClient googleTokenClient;

    @Mock
    private GoogleUserClient googleUserClient;

    private OAuthRequest testOAuthRequest;
    private GoogleTokenResponse testTokenResponse;
    private GoogleUserInfo testGoogleUserInfo;
    private GoogleOAuth2UserInfo testOAuth2UserInfo;
    private Member existingMember;
    private Member newMember;

    private static final String TEST_CODE = "test_auth_code_12345";
    private static final String TEST_ACCESS_TOKEN = "ya29.test_access_token";
    private static final String TEST_REFRESH_TOKEN = "1//test_refresh_token";
    private static final String TEST_JWT_ACCESS_TOKEN = "jwt.access.token";
    private static final String TEST_JWT_REFRESH_TOKEN = "jwet.refresh.token";
    private static final String TEST_CLIENT_ID = "test_client_id.googleusercontent.com";
    private static final String TEST_CLIENT_SECRET = "test_client_secret";
    private static final String TEST_REDIRECT_URI = "http://localhost:8080/oauth2/callback/google";
    private static final String TEST_USER_ID = "google_user_id_123";
    private static final String TEST_EMAIL = "test@gmail.com";
    private static final String TEST_NAME = "Test User";
    private static final String TEST_NICKNAME = "testuser";
    private static final String TEST_PICTURE_URL = "https://lh3.googleusercontent.com/test_picture";
    private static final Long TEST_MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        // OAuth 설정값 주입
        ReflectionTestUtils.setField(googleOAuth2UserService, "clientId", TEST_CLIENT_ID);
        ReflectionTestUtils.setField(googleOAuth2UserService, "clientSecret", TEST_CLIENT_SECRET);
        ReflectionTestUtils.setField(googleOAuth2UserService, "redirectUri", TEST_REDIRECT_URI);

        // Test OAuthRequest setup
        testOAuthRequest = new OAuthRequest();
        testOAuthRequest.setCode(TEST_CODE);
        testOAuthRequest.setProvider(OauthProvider.GOOGLE);

        // Test GoogleTokenResponse setup
        testTokenResponse = new GoogleTokenResponse();
        testTokenResponse.setAccessToken(TEST_ACCESS_TOKEN);
        testTokenResponse.setTokenType("Bearer");
        testTokenResponse.setExpiresIn(3600L);

        // Test GoogleUserInfo setup
        testGoogleUserInfo = new GoogleUserInfo(
            TEST_USER_ID,
            TEST_EMAIL,
            TEST_NAME,
            TEST_PICTURE_URL
        );

        // Test GoogleOAuth2UserInfo setup
        testOAuth2UserInfo = new GoogleOAuth2UserInfo(testGoogleUserInfo);

        // Test Member setup - 기존 멤버
        existingMember = Member.builder()
            .id(TEST_MEMBER_ID)
            .email(TEST_EMAIL)
            .nickname(TEST_NICKNAME)
            .memberName(TEST_NAME)
            .profileImg(TEST_PICTURE_URL)
            .provider(OauthProvider.GOOGLE)
            .providerId(TEST_USER_ID)
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
            when(googleTokenClient.getAccessToken(any())).thenReturn(testTokenResponse);

            // When
            String accessToken = googleOAuth2UserService.getAccessToken(testOAuthRequest);

            // Then
            assertThat(accessToken).isEqualTo(TEST_ACCESS_TOKEN);

            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(googleTokenClient).getAccessToken(paramsCaptor.capture());
            
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
            when(googleTokenClient.getAccessToken(any())).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> googleOAuth2UserService.getAccessToken(testOAuthRequest))
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
            longCodeRequest.setProvider(OauthProvider.GOOGLE);
            when(googleTokenClient.getAccessToken(any())).thenReturn(testTokenResponse);

            // When
            String accessToken = googleOAuth2UserService.getAccessToken(longCodeRequest);

            // Then
            assertThat(accessToken).isEqualTo(TEST_ACCESS_TOKEN);
            
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(googleTokenClient).getAccessToken(paramsCaptor.capture());
            
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
            when(googleUserClient.getUserInfo("Bearer " + TEST_ACCESS_TOKEN))
                .thenReturn(testGoogleUserInfo);

            // When
            OAuth2UserInfo userInfo = googleOAuth2UserService.getUserInfo(TEST_ACCESS_TOKEN);

            // Then
            assertThat(userInfo).isInstanceOf(GoogleOAuth2UserInfo.class);
            assertThat(userInfo.getId()).isEqualTo(TEST_USER_ID);
            assertThat(userInfo.getName()).isEqualTo(TEST_NAME);
            assertThat(userInfo.getImageUrl()).isEqualTo(TEST_PICTURE_URL);

            verify(googleUserClient).getUserInfo("Bearer " + TEST_ACCESS_TOKEN);
        }

        @Test
        @DisplayName("사용자 정보 응답이 null인 경우 예외를 발생시킨다")
        void getUserInfo_WhenUserInfoIsNull_ThrowsException() {
            // Given
            when(googleUserClient.getUserInfo(anyString())).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> googleOAuth2UserService.getUserInfo(TEST_ACCESS_TOKEN))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("사용자 정보가 비어있습니다.");
        }

        @Test
        @DisplayName("Bearer 토큰 형식이 올바르게 설정된다")
        void getUserInfo_SetsBearerTokenFormatCorrectly() {
            // Given
            String customAccessToken = "custom_access_token_123";
            when(googleUserClient.getUserInfo("Bearer " + customAccessToken))
                .thenReturn(testGoogleUserInfo);

            // When
            googleOAuth2UserService.getUserInfo(customAccessToken);

            // Then
            verify(googleUserClient).getUserInfo("Bearer " + customAccessToken);
        }
    }

    @Nested
    @DisplayName("createOrUpdateMember Tests")
    class CreateOrUpdateMemberTests {

        @Test
        @DisplayName("기존 멤버가 있는 경우 업데이트한다")
        void createOrUpdateMember_WhenMemberExists_UpdatesMember() {
            // Given
            when(memberRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, TEST_USER_ID))
                .thenReturn(Optional.of(existingMember));
            when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

            // When
            Member result = googleOAuth2UserService.createOrUpdateMember(testOAuth2UserInfo);

            // Then
            assertThat(result).isEqualTo(existingMember);
            verify(memberRepository).findByProviderAndProviderId(OauthProvider.GOOGLE, TEST_USER_ID);
            verify(memberRepository).save(existingMember);
            
            // update 메소드가 호출되었는지는 실제 구현에 따라 검증이 어려우므로 save 호출만 확인
        }

        @Test
        @DisplayName("새 멤버인 경우 새로 생성한다")
        void createOrUpdateMember_WhenMemberNotExists_CreatesNewMember() {
            // Given
            when(memberRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, TEST_USER_ID))
                .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(newMember);

            // When
            Member result = googleOAuth2UserService.createOrUpdateMember(testOAuth2UserInfo);

            // Then
            assertThat(result).isEqualTo(newMember);
            verify(memberRepository).findByProviderAndProviderId(OauthProvider.GOOGLE, TEST_USER_ID);
            
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository).save(memberCaptor.capture());
            
            Member capturedMember = memberCaptor.getValue();
            // 새 Member 객체가 저장되었는지 확인
            assertThat(capturedMember).isNotNull();
        }

    }

    @Nested
    @DisplayName("setBearerAuth method Tests")
    class SetBearerAuthTests {

        @Test
        @DisplayName("액세스 토큰에 Bearer 접두사를 올바르게 추가한다")
        void setBearerAuth_AddsCorrectPrefix() {
            // Given - getUserInfo 메소드를 통해 setBearerAuth 테스트
            when(googleUserClient.getUserInfo("Bearer " + TEST_ACCESS_TOKEN))
                .thenReturn(testGoogleUserInfo);

            // When
            OAuth2UserInfo userInfo = googleOAuth2UserService.getUserInfo(TEST_ACCESS_TOKEN);

            // Then
            assertThat(userInfo).isNotNull();
            verify(googleUserClient).getUserInfo("Bearer " + TEST_ACCESS_TOKEN);
        }

    }
}