package com.mylog.service.social.naver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.api.auth.service.social.naver.NaverOAuth2UserService;
import com.mylog.api.auth.service.social.naver.NaverTokenClient;
import com.mylog.api.auth.service.social.naver.NaverUserClient;
import com.mylog.api.auth.JwtUtil;
import com.mylog.common.enums.OauthProvider;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.api.auth.dto.social.OAuth2UserInfo;
import com.mylog.api.auth.dto.social.OAuthRequest;
import com.mylog.api.auth.dto.social.naver.NaverOAuth2UserInfo;
import com.mylog.api.auth.dto.social.naver.NaverResponse;
import com.mylog.api.auth.dto.social.naver.NaverTokenResponse;
import com.mylog.api.auth.dto.social.naver.NaverUserInfo;
import com.mylog.api.member.entity.Member;
import com.mylog.api.member.repository.MemberRepository;
import com.mylog.api.auth.service.RefreshTokenService;
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
 * Comprehensive unit tests for NaverOAuth2UserService
 * Tests OAuth2 flow: token exchange, user info retrieval, member creation/update
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("NaverOAuth2UserService Unit Tests")
public class NaverOAuth2UserServiceTest {

    @InjectMocks
    private NaverOAuth2UserService naverOAuth2UserService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private NaverTokenClient naverTokenClient;

    @Mock
    private NaverUserClient naverUserClient;

    private OAuthRequest testOAuthRequest;
    private NaverTokenResponse testTokenResponse;
    private NaverUserInfo testNaverUserInfo;
    private NaverOAuth2UserInfo testOAuth2UserInfo;
    private Member existingMember;
    private Member newMember;
    private NaverResponse testNaverResponse;

    private static final String TEST_CODE = "test_naver_auth_code_12345";
    private static final String TEST_STATE = "test_naver_state_abcdef";
    private static final String TEST_ACCESS_TOKEN = "naver_access_token_example";
    private static final String TEST_REFRESH_TOKEN = "naver_refresh_token_example";
    private static final String TEST_CLIENT_ID = "test_naver_client_id";
    private static final String TEST_CLIENT_SECRET = "test_naver_client_secret";
    private static final String TEST_REDIRECT_URI = "http://localhost:8080/oauth2/callback/naver";
    private static final String TEST_USER_ID = "naver_user_123456";
    private static final String TEST_USER_NAME = "네이버사용자";
    private static final String TEST_NICKNAME = "testuser";
    private static final String TEST_PROFILE_IMAGE = "https://ssl.pstatic.net/static/pwe/address/img_profile.png";
    private static final Long TEST_MEMBER_ID = 1L;

    @BeforeEach
    void setUp() {
        // OAuth 설정값 주입
        ReflectionTestUtils.setField(naverOAuth2UserService, "clientId", TEST_CLIENT_ID);
        ReflectionTestUtils.setField(naverOAuth2UserService, "clientSecret", TEST_CLIENT_SECRET);
        ReflectionTestUtils.setField(naverOAuth2UserService, "redirectUri", TEST_REDIRECT_URI);

        // Test OAuthRequest setup
        testOAuthRequest = new OAuthRequest();
        testOAuthRequest.setCode(TEST_CODE);
        testOAuthRequest.setState(TEST_STATE);
        testOAuthRequest.setProvider(OauthProvider.NAVER);

        // Test NaverTokenResponse setup
        testTokenResponse = new NaverTokenResponse();
        testTokenResponse.setAccessToken(TEST_ACCESS_TOKEN);
        testTokenResponse.setTokenType("bearer");
        testTokenResponse.setRefreshToken(TEST_REFRESH_TOKEN);

        // Test NaverResponse setup
        testNaverResponse = new NaverResponse(TEST_USER_ID, TEST_NICKNAME, TEST_USER_NAME, TEST_PROFILE_IMAGE);

        // Test NaverUserInfo setup
        testNaverUserInfo = new NaverUserInfo("00", "success", testNaverResponse);

        // Test NaverOAuth2UserInfo setup
        testOAuth2UserInfo = new NaverOAuth2UserInfo(testNaverUserInfo);

        // Test Member setup - 기존 멤버
        existingMember = Member.builder()
            .id(TEST_MEMBER_ID)
            .nickname(TEST_NICKNAME)
            .memberName(TEST_USER_NAME)
            .profileImg(TEST_PROFILE_IMAGE)
            .provider(OauthProvider.NAVER)
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
            when(naverTokenClient.getAccessToken(any())).thenReturn(testTokenResponse);

            // When
            String accessToken = naverOAuth2UserService.getAccessToken(testOAuthRequest);

            // Then
            assertThat(accessToken).isEqualTo(TEST_ACCESS_TOKEN);

            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(naverTokenClient).getAccessToken(paramsCaptor.capture());
            
            Map<String, String> capturedParams = paramsCaptor.getValue();
            assertThat(capturedParams).containsEntry("code", TEST_CODE);
            assertThat(capturedParams).containsEntry("state", TEST_STATE);
            assertThat(capturedParams).containsEntry("client_id", TEST_CLIENT_ID);
            assertThat(capturedParams).containsEntry("client_secret", TEST_CLIENT_SECRET);
            assertThat(capturedParams).containsEntry("redirect_uri", TEST_REDIRECT_URI);
            assertThat(capturedParams).containsEntry("grant_type", "authorization_code");
        }

        @Test
        @DisplayName("토큰 응답이 null인 경우 예외를 발생시킨다")
        void getAccessToken_WhenTokenResponseIsNull_ThrowsException() {
            // Given
            when(naverTokenClient.getAccessToken(any())).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> naverOAuth2UserService.getAccessToken(testOAuthRequest))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("토큰 응답이 비어있습니다.");
        }

        @Test
        @DisplayName("액세스 토큰이 null인 경우 예외를 발생시킨다")
        void getAccessToken_WhenAccessTokenIsNull_ThrowsException() {
            // Given
            NaverTokenResponse nullTokenResponse = new NaverTokenResponse();
            nullTokenResponse.setAccessToken(null);
            when(naverTokenClient.getAccessToken(any())).thenReturn(nullTokenResponse);

            // When & Then
            assertThatThrownBy(() -> naverOAuth2UserService.getAccessToken(testOAuthRequest))
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
            longCodeRequest.setState(TEST_STATE);
            longCodeRequest.setProvider(OauthProvider.NAVER);
            when(naverTokenClient.getAccessToken(any())).thenReturn(testTokenResponse);

            // When
            String accessToken = naverOAuth2UserService.getAccessToken(longCodeRequest);

            // Then
            assertThat(accessToken).isEqualTo(TEST_ACCESS_TOKEN);
            
            ArgumentCaptor<Map<String, String>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
            verify(naverTokenClient).getAccessToken(paramsCaptor.capture());
            
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
            when(naverUserClient.getUserInfo("Bearer " + TEST_ACCESS_TOKEN))
                .thenReturn(testNaverUserInfo);

            // When
            OAuth2UserInfo userInfo = naverOAuth2UserService.getUserInfo(TEST_ACCESS_TOKEN);

            // Then
            assertThat(userInfo).isInstanceOf(NaverOAuth2UserInfo.class);
            assertThat(userInfo.getId()).isEqualTo(TEST_USER_ID);
            assertThat(userInfo.getName()).isEqualTo(TEST_USER_NAME);
            assertThat(userInfo.getImageUrl()).isEqualTo(TEST_PROFILE_IMAGE);

            verify(naverUserClient).getUserInfo("Bearer " + TEST_ACCESS_TOKEN);
        }

        @Test
        @DisplayName("사용자 정보 응답이 null인 경우 예외를 발생시킨다")
        void getUserInfo_WhenUserInfoIsNull_ThrowsException() {
            // Given
            when(naverUserClient.getUserInfo(anyString())).thenReturn(null);

            // When & Then
            assertThatThrownBy(() -> naverOAuth2UserService.getUserInfo(TEST_ACCESS_TOKEN))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("사용자 정보가 비어있습니다.");
        }

        @Test
        @DisplayName("사용자 정보의 response가 null인 경우 예외를 발생시킨다")
        void getUserInfo_WhenUserInfoResponseIsNull_ThrowsException() {
            // Given
            NaverUserInfo nullResponseUserInfo = new NaverUserInfo("00", "success", null);
            when(naverUserClient.getUserInfo(anyString())).thenReturn(nullResponseUserInfo);

            // When & Then
            assertThatThrownBy(() -> naverOAuth2UserService.getUserInfo(TEST_ACCESS_TOKEN))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("사용자 정보가 비어있습니다.");
        }

        @Test
        @DisplayName("Bearer 토큰 형식이 올바르게 설정된다")
        void getUserInfo_SetsBearerTokenFormatCorrectly() {
            // Given
            String customAccessToken = "custom_naver_token_123";
            when(naverUserClient.getUserInfo("Bearer " + customAccessToken))
                .thenReturn(testNaverUserInfo);

            // When
            naverOAuth2UserService.getUserInfo(customAccessToken);

            // Then
            verify(naverUserClient).getUserInfo("Bearer " + customAccessToken);
        }

        @Test
        @DisplayName("Naver API 에러 응답 코드에 대해 예외를 발생시킨다")
        void getUserInfo_WhenNaverApiReturnsErrorCode_ThrowsException() {
            // Given
            NaverUserInfo errorUserInfo = new NaverUserInfo("01", "error", testNaverResponse);
            when(naverUserClient.getUserInfo(anyString())).thenReturn(errorUserInfo);

            // When
            OAuth2UserInfo userInfo = naverOAuth2UserService.getUserInfo(TEST_ACCESS_TOKEN);

            // Then
            assertThat(userInfo).isInstanceOf(NaverOAuth2UserInfo.class);
            assertThat(userInfo.getId()).isEqualTo(TEST_USER_ID);
        }
    }

    @Nested
    @DisplayName("createOrUpdateMember Tests")
    class CreateOrUpdateMemberTests {

        @Test
        @DisplayName("기존 멤버가 있는 경우 업데이트한다")
        void createOrUpdateMember_WhenMemberExists_UpdatesMember() {
            // Given
            when(memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, TEST_USER_ID))
                .thenReturn(Optional.of(existingMember));
            when(memberRepository.save(any(Member.class))).thenReturn(existingMember);

            // When
            Member result = naverOAuth2UserService.createOrUpdateMember(testOAuth2UserInfo);

            // Then
            assertThat(result).isEqualTo(existingMember);
            verify(memberRepository).findByProviderAndProviderId(OauthProvider.NAVER, TEST_USER_ID);
            verify(memberRepository).save(existingMember);
        }

        @Test
        @DisplayName("새 멤버인 경우 새로 생성한다")
        void createOrUpdateMember_WhenMemberNotExists_CreatesNewMember() {
            // Given
            when(memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, TEST_USER_ID))
                .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(newMember);

            // When
            Member result = naverOAuth2UserService.createOrUpdateMember(testOAuth2UserInfo);

            // Then
            assertThat(result).isEqualTo(newMember);
            verify(memberRepository).findByProviderAndProviderId(OauthProvider.NAVER, TEST_USER_ID);
            
            ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
            verify(memberRepository).save(memberCaptor.capture());
            
            Member capturedMember = memberCaptor.getValue();
            assertThat(capturedMember).isNotNull();
        }


        @Test
        @DisplayName("긴 사용자 ID를 가진 사용자도 처리한다")
        void createOrUpdateMember_WithLongUserId_ProcessesNormally() {
            // Given
            String longUserId = "very_long_naver_user_id_" + "a".repeat(100);
            NaverResponse longIdResponse = new NaverResponse(longUserId, TEST_NICKNAME, TEST_USER_NAME, TEST_PROFILE_IMAGE);
            NaverUserInfo longIdUserInfo = new NaverUserInfo("00", "success", longIdResponse);
            NaverOAuth2UserInfo longIdOAuth2UserInfo = new NaverOAuth2UserInfo(longIdUserInfo);

            when(memberRepository.findByProviderAndProviderId(OauthProvider.NAVER, longUserId))
                .thenReturn(Optional.empty());
            when(memberRepository.save(any(Member.class))).thenReturn(newMember);

            // When
            Member result = naverOAuth2UserService.createOrUpdateMember(longIdOAuth2UserInfo);

            // Then
            assertThat(result).isEqualTo(newMember);
            verify(memberRepository).findByProviderAndProviderId(OauthProvider.NAVER, longUserId);
        }

    }

}