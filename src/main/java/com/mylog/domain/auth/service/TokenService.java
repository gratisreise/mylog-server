package com.mylog.domain.auth.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.common.security.JwtProvider;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.dto.response.RefreshResponse;
import com.mylog.external.redis.RedisService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtProvider jwtProvider;
    private final RedisService redisService;


    //로그인 토큰 생성
    public LoginResponse generateToken(long  memberId){
        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);
        redisService.saveRefreshToken(memberId, refreshToken);
        return new LoginResponse(accessToken, refreshToken);
    }

    //토큰재발급
    public RefreshResponse reissueToken(String token) {
        if(redisService.isBlacklisted(token)){
            throw new BusinessException(ErrorCode.TOKEN_INVALID);
        }
        long memberId = jwtProvider.getRefreshMemberId(token);
        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);
        redisService.addBlacklist(token, jwtProvider.getRefreshExpiration(token));
        return new RefreshResponse(accessToken, refreshToken);
    }
}
