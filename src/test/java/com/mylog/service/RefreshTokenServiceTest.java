package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.lettuce.core.RedisConnectionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;


@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {
    private static final String TEST_EMAIL = "test@example.com";
    private static final String VALID_TOKEN = "valid.refresh.token";
    private static final String KEY_PREFIX = "refreshToken:";
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7L;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void 리프레시토큰_저장_성공(){
        //given
        String expectedKey = KEY_PREFIX + TEST_EMAIL;

        //when
        refreshTokenService.saveRefreshToken(TEST_EMAIL, VALID_TOKEN);

        //then
        verify(valueOperations).set(
            eq(expectedKey),
            eq(VALID_TOKEN),
            eq(REFRESH_TOKEN_EXPIRE_TIME),
            eq(TimeUnit.DAYS));
    }

    @Test
    void 리프레시토큰_검증_성공(){
        //given
        String expectedKey = KEY_PREFIX + TEST_EMAIL;
        when(valueOperations.get(expectedKey)).thenReturn(VALID_TOKEN);

        //when
        boolean result = refreshTokenService.validateRefreshToken(TEST_EMAIL, VALID_TOKEN);

        //then
        assertThat(result).isTrue();
        verify(valueOperations).get(expectedKey);
    }

    @Test
    void 리프레시토큰_검증_실패_토큰불일치(){
        //given
        String expectedKey = KEY_PREFIX + TEST_EMAIL;
        String invalidToken = "invalid.token";
        when(valueOperations.get(expectedKey)).thenReturn(VALID_TOKEN);

        //when
        boolean result = refreshTokenService.validateRefreshToken(TEST_EMAIL, invalidToken);

        //then
        assertThat(result).isFalse();
        verify(valueOperations).get(expectedKey);
    }


    @Test
    void 리프레시토큰_검증_실패_저장된토큰없음(){
        // given
        String expectedKey = KEY_PREFIX + TEST_EMAIL;
        when(valueOperations.get(expectedKey)).thenReturn(null);

        //when
        boolean result = refreshTokenService.validateRefreshToken(TEST_EMAIL, VALID_TOKEN);

        //then
        assertThat(result).isFalse();
        verify(valueOperations).get(expectedKey);
    }


}