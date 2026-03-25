package com.mylog.domain.auth.service;

import com.mylog.common.CommonValue;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.common.security.JwtProvider;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.dto.response.RefreshResponse;
import com.mylog.external.redis.RedisTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
  private final JwtProvider jwtProvider;
  private final RedisTokenService redisTokenService;

  // 로그인 토큰 생성
  public LoginResponse generateToken(long memberId) {
    String accessToken = jwtProvider.createAccessToken(memberId);
    String refreshToken = jwtProvider.createRefreshToken(memberId);
    redisTokenService.saveRefreshToken(memberId, refreshToken);
    return LoginResponse.of(accessToken, refreshToken);
  }

  // 토큰재발급
  public RefreshResponse reissueToken(String refreshToken) {
    // 1. 토큰 검증 & memberId 추출
    jwtProvider.validateRefreshToken(refreshToken);

    // 2. memberId 추출
    Long memberId = jwtProvider.getRefreshMemberId(refreshToken);

    // 3. Redis 대조 (저장된 RT와 일치하는지)
    String storedRT = redisTokenService.getRefreshToken(memberId);

    if (!refreshToken.equals(storedRT)) {
      throw new BusinessException(ErrorCode.TOKEN_INVALID);
    }

    // 4. 기존 RT 삭제 (블랙리스트 대신)
    redisTokenService.deleteRefreshToken(memberId);

    // 5. 새 토큰 발급
    String newAT = jwtProvider.createAccessToken(memberId);
    String newRT = jwtProvider.createRefreshToken(memberId);
    redisTokenService.saveRefreshToken(memberId, newRT);

    return RefreshResponse.of(newAT, newRT);
  }

  public static String extractToken(String token) {
    return token.replace(CommonValue.AUTH_PREFIX, "");
  }
}
