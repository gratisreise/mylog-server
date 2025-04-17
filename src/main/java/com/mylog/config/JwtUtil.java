package com.mylog.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey accessKey;
    private final SecretKey refreshKey;
    private final long accessValidity;
    private final long refreshValidity;



    public JwtUtil(
        @Value("${jwt.access_secret}") String accessKey,
        @Value("${jwt.refresh_secret}") String refreshKey,
        @Value("${jwt.access_expiration}") long accessValidity,
        @Value("${jwt.refresh_expiration}") long refreshValidity
    ) {
        // HMAC SHA-512 알고리즘을 사용하는 키 생성
        this.accessKey = Keys.hmacShaKeyFor(accessKey.getBytes());
        this.refreshKey = Keys.hmacShaKeyFor(refreshKey.getBytes());
        this.accessValidity = accessValidity;
        this.refreshValidity = refreshValidity;
    }


    public String createAccessToken(String subject, long member_id) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessValidity);

        return Jwts.builder()
            .subject(subject)
            .claim("member_id", member_id)
            .issuedAt(now)
            .expiration(validity)
            .signWith(accessKey, Jwts.SIG.HS512) // 0.12.x 버전의 새로운 서명 방식
            .compact();
    }

    public String createRefreshToken(String subject) {
        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshValidity);

        return Jwts.builder()
            .subject(subject)
            .issuedAt(now)
            .expiration(validity)
            .signWith(refreshKey, SIG.HS512)
            .compact();
    }

    public String getUsername(String token) {
        return Jwts.parser()
            .verifyWith(accessKey)  // 0.12.x 버전의 새로운 검증 방식, 유요한지?, 만료되었는지
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }

    public Long getMemberId(String token){
        return Jwts.parser()
            .verifyWith(accessKey)
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("member_id", Long.class);
    }


    public boolean validateAccessToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}