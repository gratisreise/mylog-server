package com.mylog.service;

import com.mylog.config.JwtUtil;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final String KEY_PREFIX = "refreshToken:";
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7L;

    private final ValueOperations<String, String> valueOperations;

    public void saveRefreshToken(String id, String refreshToken) {
        valueOperations.set(
            generateKey(id),
            refreshToken,
            REFRESH_TOKEN_EXPIRE_TIME,
            TimeUnit.DAYS
        );
    }

    public boolean validateRefreshToken(String memberId, String refreshToken) {
        String storedToken = valueOperations.get(generateKey(memberId));
        return storedToken != null && storedToken.equals(refreshToken);
    }

    private String generateKey(String email) {
        return KEY_PREFIX + email;
    }


}
