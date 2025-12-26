package com.mylog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mylog.api.auth.service.RefreshTokenService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ValueOperations;

/**
 * Comprehensive unit tests for RefreshTokenService
 * Tests Redis-based refresh token storage, validation, and TTL management
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RefreshTokenService Unit Tests")
public class RefreshTokenServiceTest {

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private static final String TEST_USERNAME = "testuser@example.com";
    private static final String TEST_REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test.token";
    private static final String DIFFERENT_REFRESH_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.different.token";
    private static final String KEY_PREFIX = "refreshToken:";
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7L;

    private String expectedKey;

    @BeforeEach
    void setUp() {
        expectedKey = KEY_PREFIX + TEST_USERNAME;
    }

    @Nested
    @DisplayName("saveRefreshToken Tests")
    class SaveRefreshTokenTests {

        @Test
        @DisplayName("리프레시 토큰을 Redis에 저장한다")
        void saveRefreshToken_SavesTokenToRedis() {
            // When
            refreshTokenService.saveRefreshToken(TEST_USERNAME, TEST_REFRESH_TOKEN);

            // Then
            verify(valueOperations).set(
                eq(expectedKey),
                eq(TEST_REFRESH_TOKEN),
                eq(REFRESH_TOKEN_EXPIRE_TIME),
                eq(TimeUnit.DAYS)
            );
        }

        @Test
        @DisplayName("다른 사용자의 리프레시 토큰을 별도 키로 저장한다")
        void saveRefreshToken_SavesDifferentUserTokenWithDifferentKey() {
            // Given
            String differentUsername = "different@example.com";
            String differentKey = KEY_PREFIX + differentUsername;

            // When
            refreshTokenService.saveRefreshToken(differentUsername, TEST_REFRESH_TOKEN);

            // Then
            verify(valueOperations).set(
                eq(differentKey),
                eq(TEST_REFRESH_TOKEN),
                eq(REFRESH_TOKEN_EXPIRE_TIME),
                eq(TimeUnit.DAYS)
            );
        }

        @Test
        @DisplayName("같은 사용자의 새로운 토큰으로 덮어쓴다")
        void saveRefreshToken_OverwritesExistingToken() {
            // When - 첫 번째 토큰 저장
            refreshTokenService.saveRefreshToken(TEST_USERNAME, TEST_REFRESH_TOKEN);
            
            // When - 같은 사용자에 대해 새 토큰 저장
            refreshTokenService.saveRefreshToken(TEST_USERNAME, DIFFERENT_REFRESH_TOKEN);

            // Then - 두 번의 저장 호출이 발생
            verify(valueOperations, times(2)).set(
                eq(expectedKey),
                anyString(),
                eq(REFRESH_TOKEN_EXPIRE_TIME),
                eq(TimeUnit.DAYS)
            );
            
            // 마지막 호출은 새 토큰으로
            verify(valueOperations).set(
                eq(expectedKey),
                eq(DIFFERENT_REFRESH_TOKEN),
                eq(REFRESH_TOKEN_EXPIRE_TIME),
                eq(TimeUnit.DAYS)
            );
        }

        @Test
        @DisplayName("빈 문자열 사용자명으로도 저장할 수 있다")
        void saveRefreshToken_HandlesEmptyUsername() {
            // Given
            String emptyUsername = "";
            String emptyKey = KEY_PREFIX + emptyUsername;

            // When
            refreshTokenService.saveRefreshToken(emptyUsername, TEST_REFRESH_TOKEN);

            // Then
            verify(valueOperations).set(
                eq(emptyKey),
                eq(TEST_REFRESH_TOKEN),
                eq(REFRESH_TOKEN_EXPIRE_TIME),
                eq(TimeUnit.DAYS)
            );
        }

        @Test
        @DisplayName("빈 문자열 토큰도 저장할 수 있다")
        void saveRefreshToken_HandlesEmptyToken() {
            // Given
            String emptyToken = "";

            // When
            refreshTokenService.saveRefreshToken(TEST_USERNAME, emptyToken);

            // Then
            verify(valueOperations).set(
                eq(expectedKey),
                eq(emptyToken),
                eq(REFRESH_TOKEN_EXPIRE_TIME),
                eq(TimeUnit.DAYS)
            );
        }

        @Test
        @DisplayName("특수 문자가 포함된 사용자명으로도 저장할 수 있다")
        void saveRefreshToken_HandlesSpecialCharactersInUsername() {
            // Given
            String specialUsername = "user+test@example-domain.co.kr";
            String specialKey = KEY_PREFIX + specialUsername;

            // When
            refreshTokenService.saveRefreshToken(specialUsername, TEST_REFRESH_TOKEN);

            // Then
            verify(valueOperations).set(
                eq(specialKey),
                eq(TEST_REFRESH_TOKEN),
                eq(REFRESH_TOKEN_EXPIRE_TIME),
                eq(TimeUnit.DAYS)
            );
        }
    }

    @Nested
    @DisplayName("validateRefreshToken Tests")
    class ValidateRefreshTokenTests {

        @Test
        @DisplayName("저장된 토큰과 일치하는 경우 true를 반환한다")
        void validateRefreshToken_WhenTokenMatches_ReturnsTrue() {
            // Given
            when(valueOperations.get(expectedKey)).thenReturn(TEST_REFRESH_TOKEN);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, TEST_REFRESH_TOKEN);

            // Then
            assertThat(isValid).isTrue();
            verify(valueOperations).get(expectedKey);
        }

        @Test
        @DisplayName("저장된 토큰과 일치하지 않는 경우 false를 반환한다")
        void validateRefreshToken_WhenTokenDoesNotMatch_ReturnsFalse() {
            // Given
            when(valueOperations.get(expectedKey)).thenReturn(TEST_REFRESH_TOKEN);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, DIFFERENT_REFRESH_TOKEN);

            // Then
            assertThat(isValid).isFalse();
            verify(valueOperations).get(expectedKey);
        }

        @Test
        @DisplayName("저장된 토큰이 없는 경우 false를 반환한다")
        void validateRefreshToken_WhenNoStoredToken_ReturnsFalse() {
            // Given
            when(valueOperations.get(expectedKey)).thenReturn(null);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, TEST_REFRESH_TOKEN);

            // Then
            assertThat(isValid).isFalse();
            verify(valueOperations).get(expectedKey);
        }

        @Test
        @DisplayName("존재하지 않는 사용자의 토큰 검증 시 false를 반환한다")
        void validateRefreshToken_WhenUserDoesNotExist_ReturnsFalse() {
            // Given
            String nonExistentUser = "nonexistent@example.com";
            String nonExistentKey = KEY_PREFIX + nonExistentUser;
            when(valueOperations.get(nonExistentKey)).thenReturn(null);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(nonExistentUser, TEST_REFRESH_TOKEN);

            // Then
            assertThat(isValid).isFalse();
            verify(valueOperations).get(nonExistentKey);
        }

        @Test
        @DisplayName("빈 문자열 토큰 검증 시 저장된 토큰과 비교한다")
        void validateRefreshToken_WithEmptyToken_ComparesWithStoredToken() {
            // Given
            String emptyToken = "";
            when(valueOperations.get(expectedKey)).thenReturn(TEST_REFRESH_TOKEN);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, emptyToken);

            // Then
            assertThat(isValid).isFalse();
            verify(valueOperations).get(expectedKey);
        }

        @Test
        @DisplayName("빈 문자열이 저장된 토큰과 빈 문자열 입력이 일치하는 경우 true를 반환한다")
        void validateRefreshToken_WhenBothTokensAreEmpty_ReturnsTrue() {
            // Given
            String emptyToken = "";
            when(valueOperations.get(expectedKey)).thenReturn(emptyToken);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, emptyToken);

            // Then
            assertThat(isValid).isTrue();
            verify(valueOperations).get(expectedKey);
        }

        @Test
        @DisplayName("null 토큰으로 검증 시 false를 반환한다")
        void validateRefreshToken_WithNullToken_ReturnsFalse() {
            // Given
            when(valueOperations.get(expectedKey)).thenReturn(TEST_REFRESH_TOKEN);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, null);

            // Then
            assertThat(isValid).isFalse();
            verify(valueOperations).get(expectedKey);
        }

        @Test
        @DisplayName("대소문자가 다른 토큰은 일치하지 않는다")
        void validateRefreshToken_CaseSensitiveComparison_ReturnsFalse() {
            // Given
            String upperCaseToken = TEST_REFRESH_TOKEN.toUpperCase();
            when(valueOperations.get(expectedKey)).thenReturn(TEST_REFRESH_TOKEN);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, upperCaseToken);

            // Then
            assertThat(isValid).isFalse();
            verify(valueOperations).get(expectedKey);
        }

        @Test
        @DisplayName("공백이 포함된 토큰은 정확히 일치해야 한다")
        void validateRefreshToken_WithWhitespace_ExactMatch() {
            // Given
            String tokenWithSpaces = " " + TEST_REFRESH_TOKEN + " ";
            when(valueOperations.get(expectedKey)).thenReturn(TEST_REFRESH_TOKEN);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, tokenWithSpaces);

            // Then
            assertThat(isValid).isFalse();
            verify(valueOperations).get(expectedKey);
        }
    }

    @Nested
    @DisplayName("Integration Tests")
    class IntegrationTests {

        @Test
        @DisplayName("토큰 저장 후 검증이 성공한다")
        void saveAndValidate_SuccessfulFlow() {
            // Given - 저장 후 검증을 위한 Mock 설정
            when(valueOperations.get(expectedKey)).thenReturn(TEST_REFRESH_TOKEN);

            // When - 토큰 저장
            refreshTokenService.saveRefreshToken(TEST_USERNAME, TEST_REFRESH_TOKEN);

            // Then - 저장 확인
            verify(valueOperations).set(
                eq(expectedKey),
                eq(TEST_REFRESH_TOKEN),
                eq(REFRESH_TOKEN_EXPIRE_TIME),
                eq(TimeUnit.DAYS)
            );

            // When - 토큰 검증
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, TEST_REFRESH_TOKEN);

            // Then - 검증 성공
            assertThat(isValid).isTrue();
            verify(valueOperations).get(expectedKey);
        }

        @Test
        @DisplayName("여러 사용자의 토큰을 독립적으로 관리한다")
        void multipleUsers_IndependentTokenManagement() {
            // Given
            String user1 = "user1@example.com";
            String user2 = "user2@example.com";
            String token1 = "token1";
            String token2 = "token2";
            String key1 = KEY_PREFIX + user1;
            String key2 = KEY_PREFIX + user2;

            when(valueOperations.get(key1)).thenReturn(token1);
            when(valueOperations.get(key2)).thenReturn(token2);

            // When - 각 사용자의 토큰 저장
            refreshTokenService.saveRefreshToken(user1, token1);
            refreshTokenService.saveRefreshToken(user2, token2);

            // When - 각 사용자의 토큰 검증
            boolean user1Valid = refreshTokenService.validateRefreshToken(user1, token1);
            boolean user2Valid = refreshTokenService.validateRefreshToken(user2, token2);
            boolean user1InvalidWithUser2Token = refreshTokenService.validateRefreshToken(user1, token2);

            // Then
            assertThat(user1Valid).isTrue();
            assertThat(user2Valid).isTrue();
            assertThat(user1InvalidWithUser2Token).isFalse();

            verify(valueOperations).set(eq(key1), eq(token1), eq(REFRESH_TOKEN_EXPIRE_TIME), eq(TimeUnit.DAYS));
            verify(valueOperations).set(eq(key2), eq(token2), eq(REFRESH_TOKEN_EXPIRE_TIME), eq(TimeUnit.DAYS));
        }

        @Test
        @DisplayName("토큰 갱신 시 이전 토큰은 무효화된다")
        void tokenRefresh_InvalidatesPreviousToken() {
            // Given
            String oldToken = "old_token";
            String newToken = "new_token";

            // When - 이전 토큰으로 검증 (Redis에서 새 토큰 반환)
            when(valueOperations.get(expectedKey)).thenReturn(newToken);
            
            boolean oldTokenValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, oldToken);
            boolean newTokenValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, newToken);

            // Then
            assertThat(oldTokenValid).isFalse();
            assertThat(newTokenValid).isTrue();
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("매우 긴 사용자명으로도 정상 작동한다")
        void validateRefreshToken_WithVeryLongUsername() {
            // Given
            String longUsername = "a".repeat(1000) + "@example.com";
            String longKey = KEY_PREFIX + longUsername;
            when(valueOperations.get(longKey)).thenReturn(TEST_REFRESH_TOKEN);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(longUsername, TEST_REFRESH_TOKEN);

            // Then
            assertThat(isValid).isTrue();
            verify(valueOperations).get(longKey);
        }

        @Test
        @DisplayName("매우 긴 토큰으로도 정상 작동한다")
        void validateRefreshToken_WithVeryLongToken() {
            // Given
            String longToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9." + "a".repeat(1000);
            when(valueOperations.get(expectedKey)).thenReturn(longToken);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, longToken);

            // Then
            assertThat(isValid).isTrue();
            verify(valueOperations).get(expectedKey);
        }

        @Test
        @DisplayName("Unicode 문자가 포함된 사용자명으로도 정상 작동한다")
        void validateRefreshToken_WithUnicodeUsername() {
            // Given
            String unicodeUsername = "사용자명@한글도메인.한국";
            String unicodeKey = KEY_PREFIX + unicodeUsername;
            when(valueOperations.get(unicodeKey)).thenReturn(TEST_REFRESH_TOKEN);

            // When
            refreshTokenService.saveRefreshToken(unicodeUsername, TEST_REFRESH_TOKEN);
            boolean isValid = refreshTokenService.validateRefreshToken(unicodeUsername, TEST_REFRESH_TOKEN);

            // Then
            assertThat(isValid).isTrue();
            verify(valueOperations).set(eq(unicodeKey), eq(TEST_REFRESH_TOKEN), eq(REFRESH_TOKEN_EXPIRE_TIME), eq(TimeUnit.DAYS));
            verify(valueOperations).get(unicodeKey);
        }

        @Test
        @DisplayName("특수 제어 문자가 포함된 토큰도 처리한다")
        void validateRefreshToken_WithSpecialControlCharacters() {
            // Given
            String tokenWithControlChars = "token\n\r\t\0";
            when(valueOperations.get(expectedKey)).thenReturn(tokenWithControlChars);

            // When
            boolean isValid = refreshTokenService.validateRefreshToken(TEST_USERNAME, tokenWithControlChars);

            // Then
            assertThat(isValid).isTrue();
            verify(valueOperations).get(expectedKey);
        }
    }

    @Nested
    @DisplayName("Key Generation Tests")
    class KeyGenerationTests {

        @Test
        @DisplayName("generateKey 메소드가 올바른 키 형식을 생성한다")
        void keyGeneration_CorrectFormat() {
            // When - saveRefreshToken 호출을 통해 키 생성 검증
            refreshTokenService.saveRefreshToken(TEST_USERNAME, TEST_REFRESH_TOKEN);

            // Then - KEY_PREFIX + username 형식으로 키가 생성됨
            verify(valueOperations).set(
                eq(KEY_PREFIX + TEST_USERNAME),
                eq(TEST_REFRESH_TOKEN),
                eq(REFRESH_TOKEN_EXPIRE_TIME),
                eq(TimeUnit.DAYS)
            );
        }

        @Test
        @DisplayName("다른 사용자명은 다른 키를 생성한다")
        void keyGeneration_DifferentUsersHaveDifferentKeys() {
            // Given
            String user1 = "user1@example.com";
            String user2 = "user2@example.com";

            // When
            refreshTokenService.saveRefreshToken(user1, TEST_REFRESH_TOKEN);
            refreshTokenService.saveRefreshToken(user2, TEST_REFRESH_TOKEN);

            // Then
            verify(valueOperations).set(eq(KEY_PREFIX + user1), anyString(), anyLong(), any(TimeUnit.class));
            verify(valueOperations).set(eq(KEY_PREFIX + user2), anyString(), anyLong(), any(TimeUnit.class));
        }
    }
}