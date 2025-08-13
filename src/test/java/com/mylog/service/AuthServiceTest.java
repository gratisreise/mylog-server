package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.config.JwtUtil;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.auth.LoginRequest;
import com.mylog.model.dto.auth.LoginResponse;
import com.mylog.model.dto.auth.RefreshRequest;
import com.mylog.model.dto.auth.RefreshResponse;
import com.mylog.model.entity.Member;
import com.mylog.service.member.MemberReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Unit Tests")
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MemberReader memberReader;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private Authentication authentication;

    private static final String TEST_ACCESS_TOKEN = "test.access.token";
    private static final String TEST_REFRESH_TOKEN = "test.refresh.token";
    private static final String NEW_ACCESS_TOKEN = "new.access.token";

    private static final String DEFAULT_EMAIL = "test@example.com";
    private static final String DEFAULT_PASSWORD = "password123";
    private static final String DEFAULT_ENCRYPTED_PASSWORD = "$2a$10$encrypted.password.hash";
    private static final String DEFAULT_NICKNAME = "testuser";

    private LoginRequest loginRequest;
    private RefreshRequest refreshRequest;
    private Member testMember;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequest(DEFAULT_EMAIL, DEFAULT_PASSWORD);
        refreshRequest = new RefreshRequest(TEST_REFRESH_TOKEN, OauthProvider.LOCAL);

        testMember = Member.builder()
                .id(1L)
                .email(DEFAULT_EMAIL)
                .nickname(DEFAULT_NICKNAME)
                .password(DEFAULT_ENCRYPTED_PASSWORD)
                .memberName("Test User")
                .bio("Test bio")
                .profileImg("https://example.com/profile.jpg")
                .provider(OauthProvider.LOCAL)
                .providerId(DEFAULT_EMAIL + OauthProvider.LOCAL)
                .build();
    }

    @Test
    @DisplayName("로그인 성공 - 유효한 자격증명으로 JWT 토큰 생성")
    void login_성공() {
        // Given
        String username = String.valueOf(testMember.getId()); // "1"
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(memberReader.getByEmail(DEFAULT_EMAIL)).thenReturn(testMember);
        when(jwtUtil.createAccessToken(username, 1L)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtUtil.createRefreshToken(username)).thenReturn(TEST_REFRESH_TOKEN);
        doNothing().when(refreshTokenService).saveRefreshToken(username, TEST_REFRESH_TOKEN);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(response.refreshToken()).isEqualTo(TEST_REFRESH_TOKEN);

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(memberReader).getByEmail(DEFAULT_EMAIL);
        verify(jwtUtil).createAccessToken(username, 1L);
        verify(jwtUtil).createRefreshToken(username);
        verify(refreshTokenService).saveRefreshToken(username, TEST_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("로그인 실패 - 잘못된 자격증명")
    void login_잘못된_자격증명_실패() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid credentials");
    }

    @Test
    @DisplayName("로그인 실패 - 존재하지 않는 사용자")
    void login_존재하지_않는_사용자_실패() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(memberReader.getByEmail(DEFAULT_EMAIL))
                .thenThrow(new CMissingDataException("사용자를 찾을 수 없습니다."));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(CMissingDataException.class)
                .hasMessage("사용자를 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("토큰 리프레시 성공 - 유효한 리프레시 토큰으로 새 액세스 토큰 발급")
    void refresh_성공() {
        // Given
        String username = "1"; // String.valueOf(memberId)
        when(jwtUtil.getRefreshUsername(TEST_REFRESH_TOKEN)).thenReturn(username);
        when(refreshTokenService.validateRefreshToken(username, TEST_REFRESH_TOKEN))
                .thenReturn(true);
        when(jwtUtil.createAccessToken(username, 1L)).thenReturn(NEW_ACCESS_TOKEN);

        // When
        RefreshResponse response = authService.refresh(refreshRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isEqualTo(NEW_ACCESS_TOKEN);
    }

    @Test
    @DisplayName("토큰 리프레시 실패 - 유효하지 않은 리프레시 토큰")
    void refresh_유효하지_않은_토큰_실패() {
        // Given
        String username = "1";
        when(jwtUtil.getRefreshUsername(TEST_REFRESH_TOKEN)).thenReturn(username);
        when(refreshTokenService.validateRefreshToken(username, TEST_REFRESH_TOKEN))
                .thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refresh(refreshRequest))
                .isInstanceOf(CInvalidDataException.class)
                .hasMessage("유효하지 않은 토큰입니다.");
    }

    @Test
    @DisplayName("토큰 리프레시 실패 - JWT에서 사용자명 추출 실패")
    void refresh_JWT_파싱_실패() {
        // Given
        String invalidToken = "invalid.jwt.token";
        RefreshRequest invalidRequest = new RefreshRequest(invalidToken, OauthProvider.LOCAL);
        when(jwtUtil.getRefreshUsername(invalidToken))
                .thenThrow(new CInvalidDataException("유효하지 않은 토큰 형식입니다."));

        // When & Then
        assertThatThrownBy(() -> authService.refresh(invalidRequest))
                .isInstanceOf(CInvalidDataException.class)
                .hasMessage("유효하지 않은 토큰 형식입니다.");
    }

    @Test
    @DisplayName("리프레시 토큰 저장 실패 처리")
    void login_리프레시_토큰_저장_실패() {
        // Given
        String username = String.valueOf(testMember.getId()); // "1"
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(memberReader.getByEmail(DEFAULT_EMAIL)).thenReturn(testMember);
        when(jwtUtil.createAccessToken(username, 1L)).thenReturn(TEST_ACCESS_TOKEN);
        when(jwtUtil.createRefreshToken(username)).thenReturn(TEST_REFRESH_TOKEN);
        doThrow(new RuntimeException("Redis connection failed"))
                .when(refreshTokenService).saveRefreshToken(username, TEST_REFRESH_TOKEN);

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Redis connection failed");
    }
}