package com.mylog.service;

import com.mylog.config.JwtUtil;
import com.mylog.dto.LoginRequest;
import com.mylog.dto.LoginResponse;
import com.mylog.dto.RefreshRequest;
import com.mylog.dto.RefreshResponse;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;


    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    private LoginRequest loginRequest;
    private RefreshRequest refreshRequest;
    private Member member;
    private String username = "testUser";
    private String email = "test@example.com";
    private String password = "password123";
    private long memberId = 123L;
    private String accessToken = "mockAccessToken";
    private String refreshToken = "mockRefreshToken";

    @BeforeEach
    void setUp() {
        // 테스트용 객체 초기화
        loginRequest = new LoginRequest(email, password);

        refreshRequest = new RefreshRequest(refreshToken, OauthProvider.LOCAL);

        member = new Member();
        member.setId(memberId);
        member.setNickname(username);
        member.setEmail(email);
    }

    @Test
    void 로그인_성공_액세스토큰과리프레시토큰반환() {
        // Given
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(member));
        when(jwtUtil.createAccessToken(username, memberId)).thenReturn(accessToken);
        when(jwtUtil.createRefreshToken(username)).thenReturn(refreshToken);
        doNothing().when(refreshTokenService).saveRefreshToken(username, refreshToken);

        // When
        LoginResponse response = authService.login(loginRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(memberRepository).findByEmail(email);
        verify(jwtUtil).createAccessToken(username, memberId);
        verify(jwtUtil).createRefreshToken(username);
        verify(refreshTokenService).saveRefreshToken(username, refreshToken);
    }

    @Test
    void 로그인_이메일없음_예외발생() {
        // Given
        when(memberRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(mock(Authentication.class));

        // When & Then
        assertThatThrownBy(() -> authService.login(loginRequest))
            .isInstanceOf(CMissingDataException.class);
        verify(memberRepository).findByEmail(email);
        verify(jwtUtil, never()).createAccessToken(anyString(), anyLong());
        verify(jwtUtil, never()).createRefreshToken(anyString());
    }

    @Test
    void 리프레시_성공_새로운액세스토큰반환() {
        // Given
        when(jwtUtil.getUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.getMemberId(refreshToken)).thenReturn(memberId);
        when(refreshTokenService.validateRefreshToken(username, refreshToken)).thenReturn(true);
        when(jwtUtil.createAccessToken(username, memberId)).thenReturn(accessToken);

        // When
        RefreshResponse response = authService.refresh(refreshRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        verify(jwtUtil).getUsername(refreshToken);
        verify(jwtUtil).getMemberId(refreshToken);
        verify(refreshTokenService).validateRefreshToken(username, refreshToken);
        verify(jwtUtil).createAccessToken(username, memberId);
    }

    @Test
    void 리프레시_유효하지않은리프레시토큰_예외발생() {
        // Given
        when(jwtUtil.getUsername(refreshToken)).thenReturn(username);
        when(jwtUtil.getMemberId(refreshToken)).thenReturn(memberId);
        when(refreshTokenService.validateRefreshToken(username, refreshToken)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> authService.refresh(refreshRequest))
            .isInstanceOf(CInvalidDataException.class)
            .hasMessage("유요하지 않은 토큰입니다.");
        verify(refreshTokenService).validateRefreshToken(username, refreshToken);
        verify(jwtUtil, never()).createAccessToken(anyString(), anyLong());
    }
}