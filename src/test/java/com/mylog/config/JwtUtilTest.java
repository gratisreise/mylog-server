package com.mylog.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private String accessSecret = "b3e376c910910db9de20d0cfe15f0a2390ee656399ac137045661788ebaff6598c2a7f28cae6f91c88e35be07448bee8e46d2eaefb668a671fca8fb30f4c6516";
    private String refreshSecret = "492e47a23b8b33b22f59c9df02432259d11a4c1c8cbcc42d8affefee5dc0bbbe4a09ef76553091ebd87df346a20cfdf4aa8b0b6b501070a74aea1ac0c1617a42";
    private long accessValidity = 1000 * 60 * 60; // 1 hour
    private long refreshValidity = 1000 * 60 * 60 * 24; // 1 day
    private String username = "testUser";
    private long memberId = 123L;

    @BeforeEach
    void setUp() {
        // JwtUtil 인스턴스 초기화
        jwtUtil = new JwtUtil(accessSecret, refreshSecret, accessValidity, refreshValidity);
    }

    @Test
    void 액세스토큰_생성_유효한토큰생성() {
        // Given
        String token = jwtUtil.createAccessToken(username, memberId);

        // When: token에서 클레임 추출
        Jws<Claims> claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(accessSecret.getBytes()))
            .build()
            .parseSignedClaims(token);

        // Then
        assertThat(claims.getPayload().getSubject()).isEqualTo(username);
        assertThat(claims.getPayload().get("memberId", Long.class)).isEqualTo(memberId);
        assertThat(claims.getPayload().getIssuedAt()).isNotNull();
        assertThat(claims.getPayload().getExpiration()).isNotNull();
    }

    @Test
    void 리프레시토큰_생성_유효한토큰생성() {
        // Given
        String token = jwtUtil.createRefreshToken(username);

        // When
        Jws<Claims> claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(refreshSecret.getBytes()))
            .build()
            .parseSignedClaims(token);

        // Then
        assertThat(claims.getPayload().getSubject()).isEqualTo(username);
        assertThat(claims.getPayload().getIssuedAt()).isNotNull();
        assertThat(claims.getPayload().getExpiration()).isNotNull();
    }

    @Test
    void 사용자이름_추출_정확한사용자이름반환() {
        // Given
        String token = jwtUtil.createAccessToken(username, memberId);

        // When
        String extractedUsername = jwtUtil.getUsername(token);

        // Then
        assertThat(extractedUsername).isEqualTo(username);
    }

    @Test
    void 멤버아이디_추출_정확한멤버아이디반환() {
        // Given
        String token = jwtUtil.createAccessToken(username, memberId);

        // When
        Long extractedMemberId = jwtUtil.getMemberId(token);

        // Then
        assertThat(extractedMemberId).isEqualTo(memberId);
    }

    @Test
    void 액세스토큰_검증_유효한토큰_참반환() {
        // Given
        String token = jwtUtil.createAccessToken(username, memberId);

        // When
        boolean isValid = jwtUtil.validateAccessToken(token);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void 액세스토큰_검증_유효하지않은토큰_거짓반환() {
        // Given
        String invalidToken = "invalid.token.string";

        // When
        boolean isValid = jwtUtil.validateAccessToken(invalidToken);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void 액세스토큰_검증_만료된토큰_거짓반환() throws InterruptedException {
        // Given: 짧은 유효 기간 설정
        jwtUtil = new JwtUtil(accessSecret, refreshSecret, 1, refreshValidity); // 1ms 유효
        String token = jwtUtil.createAccessToken(username, memberId);

        // When: 토큰 만료를 위해 잠시 대기
        Thread.sleep(2);

        // Then
        assertThat(jwtUtil.validateAccessToken(token)).isFalse();
    }

    @Test
    void 액세스토큰_검증_잘못된키_거짓반환() {
        // Given
        String token = jwtUtil.createAccessToken(username, memberId);
        JwtUtil wrongKeyJwtUtil = new JwtUtil("wrongSecretKeyForTestingWithSufficientLength", refreshSecret, accessValidity, refreshValidity);

        // When
        boolean isValid = wrongKeyJwtUtil.validateAccessToken(token);

        // Then
        assertThat(isValid).isFalse();
    }
}
