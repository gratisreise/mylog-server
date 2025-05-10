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
    private final RedisTemplate<String, String> redisTemplate;
    private final ValueOperations<String, String> valueOperations;


    // 리프레쉬토큰 저장
    public void saveRefreshToken(String email, String refreshToken){
        redisTemplate.opsForValue().set(generateKey(email), refreshToken, 7, TimeUnit.DAYS);
    }

    //리프레쉬 토큰검증
    public boolean validateRefreshToken(String email, String refreshToken){
        String redisRefreshToken = redisTemplate.opsForValue().get(generateKey(email));
        return redisRefreshToken != null && redisRefreshToken.equals(refreshToken);
    }

    //키생성
    private String generateKey(String email){
        return "refreshToken:" + email;
    }

}
