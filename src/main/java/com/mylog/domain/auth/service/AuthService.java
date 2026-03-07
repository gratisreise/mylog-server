package com.mylog.domain.auth.service;


import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.auth.dto.request.LoginRequest;
import com.mylog.domain.auth.dto.request.RefreshRequest;
import com.mylog.domain.auth.dto.request.SignUpRequest;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.dto.response.RefreshResponse;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.member.service.MemberWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final MemberReader memberReader;
    private final TokenService tokenService;
    private final PasswordEncoder encoder;
    private final MemberWriter memberWriter;

    //회원가입
    public void signUp(SignUpRequest request) {
        validateDuplicateMember(request.email());
        Member member = request.toEntity(encoder);
        memberWriter.save(member);
    }

    //로그인
    public LoginResponse login(LoginRequest request) {
        Member member = memberReader.getByEmail(request.email());
        member.validatePassword(request.password(), encoder);
        return tokenService.generateToken(member.getId());
    }

    //리프레쉬
    public RefreshResponse refresh(RefreshRequest request) {
        String refreshToken = request.refreshToken();
        return tokenService.reissueToken(refreshToken);
    }

    //로그아웃
    public void logout(String authHeader, Long memberId) {

    }

    private void validateDuplicateMember(String email) {
        if(memberReader.existsByEmail(email)){
            throw new BusinessException(ErrorCode.MEMBER_EMAIL_ALREADY_EXISTS);
        }
    }
}
