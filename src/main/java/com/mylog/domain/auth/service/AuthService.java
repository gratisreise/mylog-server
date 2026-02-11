package com.mylog.domain.auth.service;

import com.mylog.common.security.JwtUtil;
import com.mylog.domain.auth.dto.request.LoginRequest;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.dto.request.RefreshRequest;
import com.mylog.domain.auth.dto.response.RefreshResponse;
import com.mylog.common.exception.CInvalidDataException;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.service.MemberReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final MemberReader memberReader;
    private final RefreshTokenService refreshTokenService;

    //로그인
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        Member member = memberReader.getByEmail(request.email());

        long memberId = member.getId();
        String username = String.valueOf(memberId);

        String refreshToken = jwtUtil.createRefreshToken(username);
        String accessToken = jwtUtil.createAccessToken(username, memberId);
        refreshTokenService.saveRefreshToken(username, refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }


    //리프레쉬
    public RefreshResponse refresh(RefreshRequest request) {
        String username = jwtUtil.getRefreshUsername(request.refreshToken());
        log.info("{}", username);
        long memberId = Long.parseLong(username);
        if (!refreshTokenService.validateRefreshToken(username, request.refreshToken())) {
            throw new CInvalidDataException("유효하지 않은 토큰입니다.");
        }

        String accessToken = jwtUtil.createAccessToken(username, memberId);
        return new RefreshResponse(accessToken);
    }


}
