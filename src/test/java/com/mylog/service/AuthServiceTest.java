package com.mylog.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.mylog.config.JwtUtil;
import com.mylog.dto.LoginRequest;
import com.mylog.dto.LoginResponse;
import com.mylog.entity.Member;
import com.mylog.repository.MemberRepository;
import java.util.Optional;
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
class AuthServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void 로그인_성공시_로그인응답_반환() {
        // given
        String email = "test@example.com";
        String password = "password123";
        String refreshToken = "mocked-refresh-token";
        String accessToken = "mocked-access-token";
        Long memberId = 1L;

        LoginRequest request = new LoginRequest(email, password);
        Member mockMember = new Member();
        mockMember.setId(memberId);
        Authentication authentication = mock(Authentication.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenReturn(authentication);
        when(jwtUtil.createRefreshToken(email)).thenReturn(refreshToken);
        when(jwtUtil.createAccessToken(email, memberId)).thenReturn(accessToken);
        when(memberRepository.findByEmail(email)).thenReturn(Optional.of(mockMember));

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(accessToken);
        assertThat(response.getRefreshToken()).isEqualTo(refreshToken);

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil).createRefreshToken(email);
        verify(jwtUtil).createAccessToken(email, memberId);
        verify(memberRepository).findByEmail(email);
        verify(refreshTokenService).saveRefreshToken(email, refreshToken);
    }

    @Test
    void 인증_실패시_예외_발생() {
        // given
        String email = "wrong@example.com";
        String password = "wrongpassword";
        LoginRequest request = new LoginRequest(email, password);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // when & then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessageContaining("Invalid credentials");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtUtil, memberRepository, refreshTokenService);
    }
}