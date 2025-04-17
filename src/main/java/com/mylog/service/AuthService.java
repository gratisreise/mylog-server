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

        //검증
        //맴버 존재하는지 확인
        //authentication 객체 생성
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Member member = createMember(request);

        String refreshToken = jwtUtil.createRefreshToken(member.getId());
        String accessToken = jwtUtil.createAccessToken(member.getId(), member.getProvider());
        refreshTokenService.saveRefreshToken(member.getId().toString(), refreshToken);

        return new LoginResponse(accessToken, refreshToken);
    }

    //리프레쉬
    public RefreshResponse refresh(RefreshRequest request) {
        String memberId = jwtUtil.getId(request.getRefreshToken());

        if (!refreshTokenService.validateRefreshToken(memberId, request.getRefreshToken())) {
            throw new CInvalidDataException("유요하지 않은 토큰입니다.");
        }

        String accessToken = jwtUtil.createAccessToken(Long.valueOf(memberId), request.getProvider());
        return new RefreshResponse(accessToken);
    }


    private Member createMember(LoginRequest request) {
        return memberRepository.findByEmail(request.getEmail())
            .orElseThrow(CMissingDataException::new);
    }

}
