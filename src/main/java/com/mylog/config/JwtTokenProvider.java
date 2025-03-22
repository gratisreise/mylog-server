package com.mylog.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey key;
    private final long tokenValidity; // 24시간

    public JwtTokenProvider(
        @Value("${jwt.secret}") String secretKey,
        @Value("${jwt.expiration}") long tokenValidity
    ) {
        // HMAC SHA-512 알고리즘을 사용하는 키 생성
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
        this.tokenValidity = tokenValidity;
    }

    public String createToken(String email) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + tokenValidity);

        return Jwts.builder()
            .subject(email)
            .issuedAt(now)
            .expiration(validity)
            .signWith(key, Jwts.SIG.HS512) // 0.12.x 버전의 새로운 서명 방식
            .compact();
    }

    public String getEmail(String token) {
        return Jwts.parser()
            .verifyWith(key)  // 0.12.x 버전의 새로운 검증 방식
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}