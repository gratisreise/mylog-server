package com.mylog.external.redis;

import com.nimbusds.oauth2.sdk.token.AccessToken;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisService {
    private final StringRedisTemplate redisTemplate;

    private static final String RT_PREFIX = "RT:";
    private static final String BL_PREFIX = "BL:";

    // [ RefreshToken ]
    public void saveRefreshToken(Long memberId, String refreshToken, Duration duration) {
        String key = RT_PREFIX + memberId;
        redisTemplate.opsForValue().set(key, refreshToken, duration);
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
