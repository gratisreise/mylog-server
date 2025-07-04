package com.mylog.service;

import com.mylog.config.JwtUtil;
import com.mylog.exception.CInvalidDataException;
import com.mylog.model.dto.auth.LoginRequest;
import com.mylog.model.dto.auth.LoginResponse;
import com.mylog.model.dto.auth.RefreshRequest;
import com.mylog.model.dto.auth.RefreshResponse;
import com.mylog.model.entity.Member;
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
    private final MemberReadService memberReadService;
    private final RefreshTokenService refreshTokenService;

    //로그인
    public LoginResponse login(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Member member = memberReadService.getByEmail(request.email());

        String username = member.getNickname();
        long memberId = member.getId();

        String refreshToken = jwtUtil.createRefreshToken(username);
        String accessToken = jwtUtil.createAccessToken(username, memberId);
        refreshTokenService.saveRefreshToken(username, refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }


    //리프레쉬
    public RefreshResponse refresh(RefreshRequest request) {
        String username = jwtUtil.getRefreshUsername(request.refreshToken());
        log.info("{}", username);
        long memberId = memberReadService.getByNickname(username).getId();

        if (!refreshTokenService.validateRefreshToken(username, request.refreshToken())) {
            throw new CInvalidDataException("유요하지 않은 토큰입니다.");
        }

        String accessToken = jwtUtil.createAccessToken(username, memberId);
        return new RefreshResponse(accessToken);
    }


}
