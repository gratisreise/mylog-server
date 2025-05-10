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
    public static final String VALID_TOKEN = "valid-token";

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Test
    void 리프레시토큰_저장_성공(){
        //given
        String email = TEST_EMAIL;
        String refreshToken = VALID_TOKEN;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        //when
        refreshTokenService.saveRefreshToken(email, refreshToken);


        //then
        verify(valueOperations).set(
            eq("refreshToken:" + email),
            eq(refreshToken),
            eq(7L),
            eq(TimeUnit.DAYS));
    }

    @Test
    void 리프레쉬토큰_저장_예외발생(){
        //given
        String email = TEST_EMAIL;
        String refreshToken = VALID_TOKEN;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        doThrow(new RedisConnectionFailureException("Redis 연결 실패"))
            .when(valueOperations)
            .set(anyString(), anyString(), anyLong(), any(TimeUnit.class));

        //when & then
        assertThatThrownBy(()-> refreshTokenService.saveRefreshToken(email, refreshToken))
            .isInstanceOf(RedisConnectionFailureException.class)
            .hasMessage("Redis 연결 실패");
    }



    @Test
    void 리프레쉬토큰_검증_동일한토큰_참반환() {
        // given
        String email = TEST_EMAIL;
        String refreshToken = VALID_TOKEN;
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refreshToken:" + email)).thenReturn(refreshToken);

        // when
        boolean result = refreshTokenService.validateRefreshToken(email, refreshToken);

        // then
        assertThat(result).isTrue();
        verify(valueOperations).get("refreshToken:" + email);
    }

    @Test
    void 토큰검증_null토큰_거짓반환() {
        // given
        String email = TEST_EMAIL;
        String refreshToken = VALID_TOKEN;

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refreshToken:" + email)).thenReturn(null);

        // when
        boolean result = refreshTokenService.validateRefreshToken(email, refreshToken);

        // then
        assertThat(result).isFalse();
        verify(valueOperations).get("refreshToken:" + email);
    }

    @Test
    void 토큰검증_서로다른토큰_거짓반환() {
        // given
        String email = TEST_EMAIL;
        String refreshToken = VALID_TOKEN;
        String differentToken = "different-token";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refreshToken:" + email)).thenReturn(differentToken);

        // when
        boolean result = refreshTokenService.validateRefreshToken(email, refreshToken);

        // then
        assertThat(result).isFalse();
        verify(valueOperations).get("refreshToken:" + email);
    }

}