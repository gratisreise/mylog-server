package com.mylog.external.redis;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisTokenService {
  private final StringRedisTemplate redisTemplate;

  private static final String RT_PREFIX = "RT:";
  private static final String BL_PREFIX = "BL:";

  // [ RefreshToken ]
  private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(7);

  public void saveRefreshToken(Long memberId, String refreshToken) {
    String key = RT_PREFIX + memberId;
    redisTemplate.opsForValue().set(key, refreshToken, REFRESH_TOKEN_DURATION);
  }

  public String getRefreshToken(Long memberId) {
    return redisTemplate.opsForValue().get(RT_PREFIX + memberId);
  }

  public void deleteRefreshToken(Long memberId) {
    redisTemplate.delete(RT_PREFIX + memberId);
  }

  // [ Blacklist ]
  public void addBlacklist(String accessToken, long remainingTime) {
    String key = BL_PREFIX + accessToken;
    redisTemplate.opsForValue().set(key, "logout", Duration.ofMillis(remainingTime));
  }

  public boolean isBlacklisted(String accessToken) {
    return redisTemplate.hasKey(BL_PREFIX + accessToken);
  }
}
