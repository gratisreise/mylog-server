package com.mylog.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.mylog.config.JwtUtil;
import com.mylog.dto.LoginRequest;
import com.mylog.dto.LoginResponse;
import com.mylog.dto.RefreshRequest;
import com.mylog.dto.RefreshResponse;
import com.mylog.dto.classes.CustomUser;
import com.mylog.entity.Member;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.repository.MemberRepository;
import com.mylog.service.AuthService;
import com.mylog.service.RefreshTokenService;
import java.util.Collections;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

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

    private Member testMember;
    private Authentication authentication;
    private CustomUser customUser;
    private static final String TEST_EMAIL = "test@test.com";
    private static final String TEST_PASSWORD = "password123!";
    private static final String TEST_REFRESH_TOKEN = "refresh.token.test";
    private static final String TEST_ACCESS_TOKEN = "access.token.test";

    @BeforeEach
    void setUp() {
        // Member 설정
        testMember = Member.builder()
            .id(1L)
            .email(TEST_EMAIL)
            .password(TEST_PASSWORD)
            .memberName("테스트유저")
            .nickname("테스트닉네임")
            .provider(OauthProvider.LOCAL)
            .build();

        // CustomUser 설정
        customUser = new CustomUser(testMember, Collections.emptyList());

        // Authentication 설정
        authentication = new UsernamePasswordAuthenticationToken(
            customUser,
            null,
            Collections.emptyList()
        );
    }

    @Test
    void 로그인_성공() {
        // given
        LoginRequest request = new LoginRequest(TEST_EMAIL, TEST_PASSWORD);

        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenReturn(authentication);
        when(jwtUtil.createRefreshToken(testMember.getId()))
            .thenReturn(TEST_REFRESH_TOKEN);
        when(jwtUtil.createAccessToken(testMember.getId(), testMember.getProvider()))
            .thenReturn(TEST_ACCESS_TOKEN);
        when(memberRepository.findByEmail(TEST_EMAIL)).thenReturn(Optional.of(testMember));

        // when
        LoginResponse response = authService.login(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);
        assertThat(response.getRefreshToken()).isEqualTo(TEST_REFRESH_TOKEN);

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtUtil).createRefreshToken(testMember.getId());
        verify(jwtUtil).createAccessToken(testMember.getId(), testMember.getProvider());
        verify(refreshTokenService).saveRefreshToken(
            testMember.getId().toString(),
            TEST_REFRESH_TOKEN
        );
    }

    @Test
    void 로그인_실패_인증실패() {
        // given
        LoginRequest request = new LoginRequest(TEST_EMAIL, "잘못된비밀번호");

        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenThrow(new BadCredentialsException("잘못된 인증 정보입니다."));

        // when & then
        assertThatThrownBy(() -> authService.login(request))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("잘못된 인증 정보입니다.");

        verify(authenticationManager).authenticate(any(Authentication.class));
        verify(jwtUtil, never()).createRefreshToken(any());
        verify(jwtUtil, never()).createAccessToken(any(), any());
        verify(refreshTokenService, never()).saveRefreshToken(any(), any());
    }

    @Test
    void 리프레시_토큰_갱신_성공() {
        // given
        String memberId = "1";
        RefreshRequest request = new RefreshRequest(TEST_REFRESH_TOKEN, OauthProvider.LOCAL);

        when(jwtUtil.getId(TEST_REFRESH_TOKEN)).thenReturn(memberId);
        when(refreshTokenService.validateRefreshToken(memberId, TEST_REFRESH_TOKEN))
            .thenReturn(true);
        when(jwtUtil.createAccessToken(1L, OauthProvider.LOCAL))
            .thenReturn(TEST_ACCESS_TOKEN);

        // when
        RefreshResponse response = authService.refresh(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo(TEST_ACCESS_TOKEN);

        verify(jwtUtil).getId(TEST_REFRESH_TOKEN);
        verify(refreshTokenService).validateRefreshToken(memberId, TEST_REFRESH_TOKEN);
        verify(jwtUtil).createAccessToken(1L, OauthProvider.LOCAL);
    }

    @Test
    void 리프레시_토큰_갱신_실패_유효하지않은토큰() {
        // given
        String memberId = "1";
        RefreshRequest request = new RefreshRequest(TEST_REFRESH_TOKEN, OauthProvider.LOCAL);

        when(jwtUtil.getId(TEST_REFRESH_TOKEN)).thenReturn(memberId);
        when(refreshTokenService.validateRefreshToken(memberId, TEST_REFRESH_TOKEN))
            .thenReturn(false);

        // when & then
        assertThatThrownBy(() -> authService.refresh(request))
            .isInstanceOf(CInvalidDataException.class)
            .hasMessage("유요하지 않은 토큰입니다.");

        verify(jwtUtil).getId(TEST_REFRESH_TOKEN);
        verify(refreshTokenService).validateRefreshToken(memberId, TEST_REFRESH_TOKEN);
        verify(jwtUtil, never()).createAccessToken(any(), any());
    }

}