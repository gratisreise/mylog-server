package com.mylog.auth.service;

import com.mylog.response.CommonValue;
import com.mylog.utils.JwtUtil;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenBlackListService {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String KEY_PREFIX = "BL:";

    public void accessTokenBlack(String authHeader){
        String accessToken = extractAccessToken(authHeader);
        String key = generateKey(accessToken);
        long expiration = jwtUtil.getExpiration(accessToken);
        redisTemplate.opsForValue().set(
            key,
            accessToken,
            Duration.ofMillis(expiration)
        );
    }

    public boolean isLogout(String username, String accessToken){
        String storedToken = redisTemplate.opsForValue().get(generateKey(username));
        return storedToken != null && storedToken.equals(accessToken);
    }

    private String generateKey(String accessToken){
        return KEY_PREFIX + accessToken;
    }
    private String extractAccessToken(String authHeader){
        return authHeader.substring(CommonValue.AUTH_PREFIX_LENGTH);
    }
}
