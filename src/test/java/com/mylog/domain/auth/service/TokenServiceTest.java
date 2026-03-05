package com.mylog.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import com.mylog.common.CommonValue;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.common.security.JwtProvider;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.dto.response.RefreshResponse;
import com.mylog.external.redis.RedisService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

  private static final Long MEMBER_ID = 1L;
  private static final String ACCESS_TOKEN = "accessToken";
  private static final String REFRESH_TOKEN = "refreshToken";
  private static final String NEW_ACCESS_TOKEN = "newAccessToken";
  private static final String NEW_REFRESH_TOKEN = "newRefreshToken";
  private static final String AUTH_HEADER = "Bearer " + ACCESS_TOKEN;

  @Mock private JwtProvider jwtProvider;
  @Mock private RedisService redisService;

  @InjectMocks private TokenService tokenService;

  @Nested
  @DisplayName("토큰 생성")
  class GenerateToken {

    @Test
    @DisplayName("AT/RT 생성 및 Redis에 RT 저장에 성공한다")
    void AT_RT_생성_및_Redis에_RT_저장에_성공한다() {
      // given
      given(jwtProvider.createAccessToken(MEMBER_ID)).willReturn(ACCESS_TOKEN);
      given(jwtProvider.createRefreshToken(MEMBER_ID)).willReturn(REFRESH_TOKEN);
      willDoNothing().given(redisService).saveRefreshToken(MEMBER_ID, REFRESH_TOKEN);

      // when
      LoginResponse response = tokenService.generateToken(MEMBER_ID);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
      assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);

      then(jwtProvider).should().createAccessToken(MEMBER_ID);
      then(jwtProvider).should().createRefreshToken(MEMBER_ID);
      then(redisService).should().saveRefreshToken(MEMBER_ID, REFRESH_TOKEN);
    }
  }

  @Nested
  @DisplayName("토큰 재발급")
  class ReissueToken {

    @Test
    @DisplayName("정상 토큰 재발급에 성공한다")
    void 정상_토큰_재발급에_성공한다() {
      // given
      given(jwtProvider.getRefreshMemberId(REFRESH_TOKEN)).willReturn(MEMBER_ID);
      given(redisService.getRefreshToken(MEMBER_ID)).willReturn(REFRESH_TOKEN);
      willDoNothing().given(redisService).deleteRefreshToken(MEMBER_ID);
      given(jwtProvider.createAccessToken(MEMBER_ID)).willReturn(NEW_ACCESS_TOKEN);
      given(jwtProvider.createRefreshToken(MEMBER_ID)).willReturn(NEW_REFRESH_TOKEN);
      willDoNothing().given(redisService).saveRefreshToken(MEMBER_ID, NEW_REFRESH_TOKEN);

      // when
      RefreshResponse response = tokenService.reissueToken(REFRESH_TOKEN);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getAccessToken()).isEqualTo(NEW_ACCESS_TOKEN);
      assertThat(response.getRefreshToken()).isEqualTo(NEW_REFRESH_TOKEN);

      then(jwtProvider).should().validateRefreshToken(REFRESH_TOKEN);
      then(jwtProvider).should().getRefreshMemberId(REFRESH_TOKEN);
      then(redisService).should().getRefreshToken(MEMBER_ID);
      then(redisService).should().deleteRefreshToken(MEMBER_ID);
      then(jwtProvider).should().createAccessToken(MEMBER_ID);
      then(jwtProvider).should().createRefreshToken(MEMBER_ID);
      then(redisService).should().saveRefreshToken(MEMBER_ID, NEW_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("유효하지 않은 RT로 재발급 시 예외가 발생한다")
    void 유효하지_않은_RT로_재발급_시_예외가_발생한다() {
      // given
      willThrow(new BusinessException(ErrorCode.TOKEN_INVALID))
          .given(jwtProvider)
          .validateRefreshToken(REFRESH_TOKEN);

      // when & then
      assertThatThrownBy(() -> tokenService.reissueToken(REFRESH_TOKEN))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.TOKEN_INVALID);

      then(jwtProvider).should().validateRefreshToken(REFRESH_TOKEN);
      then(redisService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("Redis에 저장된 RT와 불일치 시 예외가 발생한다")
    void Redis에_저장된_RT와_불일치_시_예외가_발생한다() {
      // given
      String storedRefreshToken = "differentRefreshToken";

      given(jwtProvider.getRefreshMemberId(REFRESH_TOKEN)).willReturn(MEMBER_ID);
      given(redisService.getRefreshToken(MEMBER_ID)).willReturn(storedRefreshToken);

      // when & then
      assertThatThrownBy(() -> tokenService.reissueToken(REFRESH_TOKEN))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.TOKEN_INVALID);

      then(jwtProvider).should().getRefreshMemberId(REFRESH_TOKEN);
      then(redisService).should().getRefreshToken(MEMBER_ID);
    }
  }

  @Nested
  @DisplayName("토큰 추출")
  class ExtractToken {

    @Test
    @DisplayName("Bearer prefix가 제거된 토큰을 반환한다")
    void Bearer_prefix가_제거된_토큰을_반환한다() {
      // given
      String tokenWithPrefix = CommonValue.AUTH_PREFIX + ACCESS_TOKEN;

      // when
      String extractedToken = TokenService.extractToken(tokenWithPrefix);

      // then
      assertThat(extractedToken).isEqualTo(ACCESS_TOKEN);
    }

    @Test
    @DisplayName("이미 prefix가 없는 토큰은 그대로 반환된다")
    void 이미_prefix가_없는_토큰은_그대로_반환된다() {
      // given
      String tokenWithoutPrefix = ACCESS_TOKEN;

      // when
      String extractedToken = TokenService.extractToken(tokenWithoutPrefix);

      // then
      assertThat(extractedToken).isEqualTo(ACCESS_TOKEN.replace(CommonValue.AUTH_PREFIX, ""));
    }
  }
}
