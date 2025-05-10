package com.mylog.service;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

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
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MemberRepository memberRepository;


    @InjectMocks
    private AuthService authService;

//    @Test
//    void 유효한로그인_토큰반환_성공() {
//        // Given
//        LoginRequest loginRequest = new LoginRequest("validemail@example.com", "validpassword");
//        Authentication authentication = mock(Authentication.class);
//        String memberId = "1";
//        Member member = new Member();
//        member.setId(Long.valueOf(memberId));
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//            .thenReturn(authentication);
//        when(jwtUtil.createAccessToken(loginRequest.getEmail(), memberId))
//            .thenReturn("mockedJwtToken");
//        when(memberRepository.findByEmail(loginRequest.getEmail()))
//            .thenReturn(Optional.of(member));
//
//        // When
//        LoginResponse token = authService.login(loginRequest);
//
//        // Then
//        assertThat(token).isEqualTo("mockedJwtToken");
//    }

    @Test
    void 로그인실패_BadCredentialsException반환(){
        //given
        LoginRequest loginRequest = new LoginRequest("invalidemail@example.com", "invalidpassword");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("유효하지 않은 정보입니다."));

        // when&then
        assertThatThrownBy(() -> authService.login(loginRequest))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("유효하지 않은 정보입니다.");

        verify(jwtUtil, never()).createAccessToken(any(), any());
    }

    @Test
    void 비어있는정보_로그인_BadCredentialsException반환(){
        //given
        LoginRequest loginRequest = new LoginRequest("", "");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid credentials"));

        // when&then
        assertThatThrownBy(() -> authService.login(loginRequest))
            .isInstanceOf(BadCredentialsException.class)
            .hasMessage("Invalid credentials");

        verify(jwtUtil, never()).createAccessToken(any(), any());
    }

}