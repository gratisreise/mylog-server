package com.mylog.service;

import com.mylog.config.JwtUtil;
import com.mylog.model.dto.auth.LoginRequest;
import com.mylog.model.dto.auth.LoginResponse;
import com.mylog.model.dto.auth.RefreshRequest;
import com.mylog.model.dto.auth.RefreshResponse;
import com.mylog.model.entity.Member;
import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
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
    private final MemberRepository memberRepository;
    private final RefreshTokenService refreshTokenService;

    //로그인
    public LoginResponse login(LoginRequest request) {
        loginAuthenticate(request);

        Member member = createMember(request);

        String username = member.getNickname();
        long memberId = member.getId();

        String refreshToken = jwtUtil.createRefreshToken(username);
        String accessToken = jwtUtil.createAccessToken(username, memberId);
        refreshTokenService.saveRefreshToken(username, refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }


    private void loginAuthenticate(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
    }

    //리프레쉬
    public RefreshResponse refresh(RefreshRequest request) {
        String username = jwtUtil.getRefreshUsername(request.getRefreshToken());
        log.info("{}", username);
        long memberId = memberRepository.findByNickname(username)
            .orElseThrow(CMissingDataException::new).getId();

        if (!refreshTokenService.validateRefreshToken(username, request.getRefreshToken())) {
            throw new CInvalidDataException("유요하지 않은 토큰입니다.");
        }

        String accessToken = jwtUtil.createAccessToken(username, memberId);
        return new RefreshResponse(accessToken);
    }


    private Member createMember(LoginRequest request) {
        return memberRepository.findByEmail(request.getEmail())
            .orElseThrow(CMissingDataException::new);
    }

}
