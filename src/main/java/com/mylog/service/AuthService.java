package com.mylog.service;

import com.mylog.config.JwtUtil;
import com.mylog.dto.LoginRequest;
import com.mylog.dto.LoginResponse;
import com.mylog.dto.RefreshRequest;
import com.mylog.dto.RefreshResponse;
import com.mylog.entity.Member;
import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
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
        String username = jwtUtil.getUsername(request.getRefreshToken());
        long memberId = jwtUtil.getMemberId(request.getRefreshToken());


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
