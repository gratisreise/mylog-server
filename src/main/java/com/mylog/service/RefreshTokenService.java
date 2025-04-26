package com.mylog.service;

import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private static final String KEY_PREFIX = "refreshToken:";
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7L;

    private final ValueOperations<String, String> valueOperations;

    public void saveRefreshToken(String username, String refreshToken) {
        valueOperations.set(
            generateKey(username),
            refreshToken,
            REFRESH_TOKEN_EXPIRE_TIME,
            TimeUnit.DAYS
        );
    }


    public boolean validateRefreshToken(String username, String refreshToken) {
        String storedToken = valueOperations.get(generateKey(username));
        return storedToken != null && storedToken.equals(refreshToken);
    }

    private String generateKey(String username) {
        return KEY_PREFIX + username;
    }


}
