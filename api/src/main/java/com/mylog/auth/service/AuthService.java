package com.mylog.auth.service;


import com.mylog.auth.dto.LoginRequest;
import com.mylog.auth.dto.LoginResponse;
import com.mylog.auth.dto.RefreshRequest;
import com.mylog.auth.dto.RefreshResponse;
import com.mylog.exception.CInvalidDataException;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import com.mylog.utils.JwtUtil;
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
        Authentication authentication = authenticationManager
            .authenticate(new UsernamePasswordAuthenticationToken
                (request.email(), request.password()));

        Long memberId = memberReader.getByEmail(request.email()).getId();
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
