package com.mylog.service;

import com.mylog.exception.CInvalidDataException;
import com.mylog.exception.CMissingDataException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private String username;
    private String refreshToken;
    private String redisKey;
    private final long expiration = 7L;

    @BeforeEach
    void setUp() {
        username = "testUser";
        refreshToken = "testToken";
        redisKey = "refreshToken:" + username;
    }


    @Test
    void 리프레시_토큰_저장_성공() {
        // Given
        doNothing().when(valueOperations).set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        // When
        refreshTokenService.saveRefreshToken(username, refreshToken);

        // Then
        verify(valueOperations).set(redisKey, refreshToken, expiration, TimeUnit.DAYS);
    }

    @Test
    void 리프레시_토큰_검증_성공() {
        // Given
        when(valueOperations.get(redisKey)).thenReturn(refreshToken);

        // When
        boolean result = refreshTokenService.validateRefreshToken(username, refreshToken);

        // Then
        assertThat(result).isTrue();
        verify(valueOperations).get(redisKey);
    }

    @Test
    void 리프레시_토큰_검증_토큰없음_거짓반환() {
        // Given
        when(valueOperations.get(redisKey)).thenReturn(null);

        // When
        boolean result = refreshTokenService.validateRefreshToken(username, refreshToken);

        // Then
        assertThat(result).isFalse();
        verify(valueOperations).get(redisKey);
    }

    @Test
    void 리프레시_토큰_검증_토큰불일치_거짓반환() {
        // Given
        String differentToken = "differentToken";
        when(valueOperations.get(redisKey)).thenReturn(differentToken);

        // When & Then
        boolean result = refreshTokenService.validateRefreshToken(username, refreshToken);
        assertThat(result).isFalse();
        verify(valueOperations).get(redisKey);
    }

}