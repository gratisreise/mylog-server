package com.mylog.auth.service;

import com.mylog.exception.common.CUnDeletedException;
import com.mylog.exception.common.CommonError;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final String KEY_PREFIX = "RT:";

    private static final long EXPIRATION_TIME = 604800000L;

    private final StringRedisTemplate redisTemplate;

    public void saveRefreshToken(String username, String refreshToken) {
        redisTemplate.opsForValue().set(
            generateKey(username),
            refreshToken,
            Duration.ofMillis(EXPIRATION_TIME)
        );
    }

    public boolean validateRefreshToken(String username, String refreshToken) {
        String storedToken = redisTemplate.opsForValue().get(generateKey(username));
        return storedToken != null && storedToken.equals(refreshToken);
    }

    public void deleteRefreshToken(String username) {
        String key = generateKey(username);
        boolean result = redisTemplate.delete(key);

        if(!result) {
            throw new CUnDeletedException(CommonError.REFRESH_TOKEN_UNDELETED);
        }
    }

    private String generateKey(String username) {
        return KEY_PREFIX + username;
    }

}
