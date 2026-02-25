package com.mylog.domain.auth.service;

import com.mylog.common.security.JwtProvider;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.external.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtProvider jwtProvider;
    private final RedisService redisService;


    public LoginResponse generateToken(long  memberId){
        String accessToken = jwtProvider.createAccessToken(memberId);
        String refreshToken = jwtProvider.createRefreshToken(memberId);
        redisService.saveRefreshToken(memberId, refreshToken);
        return new LoginResponse(accessToken, refreshToken);
    }

}
