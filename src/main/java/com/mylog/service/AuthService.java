package com.mylog.service;

import com.mylog.config.JwtUtil;
import com.mylog.dto.LoginRequest;
import com.mylog.dto.LoginResponse;
import com.mylog.dto.RefreshRequest;
import com.mylog.dto.RefreshResponse;
import com.mylog.entity.Member;
import com.mylog.exception.CInvalidDataException;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;

    //로그인
    public LoginResponse login(LoginRequest request) {
        //컨텍스트 저장
        saveUserInfoToSecurityContext(request);
        //리프레쉬 토큰저장
        String refreshToken = jwtUtil.createRefreshToken(request.getEmail());
        refreshTokenService.saveRefreshToken(request.getEmail(), refreshToken);
        //로그인 응답반환
        return createLoginResponse(request, refreshToken);
    }

    //액세스 리프레쉬
    public RefreshResponse refresh(RefreshRequest request) {
        String email = jwtUtil.getEmail(request.getRefreshToken());
        validateRefreshToken(request, email);
        return createRefreshResponse(email);
    }


    private void saveUserInfoToSecurityContext(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private RefreshResponse createRefreshResponse(String email) {
        Long memberId = memberRepository.findByEmail(email).orElseThrow().getId();
        String accessToken = jwtUtil.createAccessToken(email, memberId);
        return new RefreshResponse(accessToken);
    }

    private void validateRefreshToken(RefreshRequest request, String email) {
        if (!refreshTokenService.validateRefreshToken(email, request.getRefreshToken())) {
            throw new CInvalidDataException("유요하지 않은 토큰입니다.");
        }
    }

    private Member createMember(LoginRequest request) {
        return memberRepository.findByEmail(request.getEmail())
            .orElseThrow();
    }

    private LoginResponse createLoginResponse(LoginRequest request, String refreshToken) {
        Long memberId = createMember(request).getId();
        return new LoginResponse(
            jwtUtil.createAccessToken(request.getEmail(), memberId),
            refreshToken
        );
    }
}
