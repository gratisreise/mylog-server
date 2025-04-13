package com.mylog.config;

import com.mylog.enums.OauthProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;

    private final String accessSecret = "5d881b56764b9d01c78899e586f980b307c5e1d5cfcb75453419949656a716cf70cb79638b1b23089826a738e37791596c54ba64a0c32bcac856c30145f11dcd";
    private final String refreshSecret = "b61a14b98e0e78707f0a9398570af81fcdb6b98665c7d3eb97e7e6bc2785bc3507a7bb04f92aac4c587d8af6b0680e1957de9b900c4d2b4037737cc14d70c41d";
    private final long accessValidity = 1000 * 60 * 60; // 1시간
    private final long refreshValidity = 1000 * 60 * 60 * 24; // 1일
    private final Long testMemberId = 1L;
    private final OauthProvider testProvider = OauthProvider.GOOGLE;
    private final String invalidToken = "9d0cbc12726312693a73489aacd2b7562ea1c176dbed048d793a99ab24896e74d77f89b7f9288b0d44cc429281e1d971286790d59c48a1b230a6a6a325da9d6c";

    @BeforeEach
    void 초기화() {
        jwtUtil = new JwtUtil(accessSecret, refreshSecret, accessValidity, refreshValidity);
    }

    @Test
    void 액세스토큰생성_유효한토큰생성() {
        String token = jwtUtil.createAccessToken(testMemberId, testProvider);

        assertThat(token).isNotNull();

        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(accessSecret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertThat(claims.getSubject()).isEqualTo(testMemberId.toString());
        assertThat(claims.get("member_id", Long.class)).isEqualTo(testMemberId);
        assertThat(claims.get("provider", String.class)).isEqualTo(testProvider.name());
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    void 리프레시토큰생성_유효한토큰생성() {
        String token = jwtUtil.createRefreshToken(testMemberId);

        assertThat(token).isNotNull();

        Claims claims = Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(refreshSecret.getBytes()))
            .build()
            .parseSignedClaims(token)
            .getPayload();

        assertThat(claims.getSubject()).isEqualTo(testMemberId.toString());
        assertThat(claims.getIssuedAt()).isNotNull();
        assertThat(claims.getExpiration()).isNotNull();
    }

    @Test
    void 아이디조회_올바른아이디반환() {
        String token = jwtUtil.createAccessToken(testMemberId, testProvider);
        String id = jwtUtil.getId(token);

        assertThat(id).isEqualTo(testMemberId.toString());
    }

    @Test
    void 아이디번호조회_올바른아이디번호반환() {
        String token = jwtUtil.createAccessToken(testMemberId, testProvider);
        Long id = jwtUtil.getIdNumber(token);

        assertThat(id).isEqualTo(testMemberId);
    }

    @Test
    void 제공자조회_올바른제공자반환() {
        String token = jwtUtil.createAccessToken(testMemberId, testProvider);
        String provider = jwtUtil.getProvider(token);

        assertThat(provider).isEqualTo(testProvider.name());
    }

    @Test
    void 액세스토큰검증_유효한토큰_참반환() {
        String token = jwtUtil.createAccessToken(testMemberId, testProvider);
        boolean isValid = jwtUtil.validateAccessToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    void 액세스토큰검증_유효하지않은토큰_거짓반환() {
        String invalidToken = "invalid.token.string";
        boolean isValid = jwtUtil.validateAccessToken(invalidToken);

        assertThat(isValid).isFalse();
    }

    @Test
    void 액세스토큰검증_만료된토큰_거짓반환() throws Exception {
        // 매우 짧은 유효 기간으로 토큰 생성
        JwtUtil shortLivedJwtUtil = new JwtUtil(accessSecret, refreshSecret, 1, refreshValidity);

        String token = shortLivedJwtUtil.createAccessToken(testMemberId, testProvider);

        // 토큰이 만료될 때까지 대기
        Thread.sleep(2);

        boolean isValid = shortLivedJwtUtil.validateAccessToken(token);
        assertThat(isValid).isFalse();
    }

    @Test
    void 액세스토큰검증_잘못된키_거짓반환() {
        // 다른 키로 토큰 생성
        SecretKey wrongKey = Keys.hmacShaKeyFor(invalidToken.getBytes());
        String token = Jwts.builder()
            .subject(testMemberId.toString())
            .signWith(wrongKey, Jwts.SIG.HS512)
            .compact();

        boolean isValid = jwtUtil.validateAccessToken(token);
        assertThat(isValid).isFalse();
    }
}