package com.mylog.domain.auth.service.oauth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mylog.common.enums.OauthProvider;
import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.auth.service.oauth.impl.GoogleOAuthService;
import com.mylog.domain.auth.service.oauth.impl.KakaoOAuthService;
import com.mylog.domain.auth.service.oauth.impl.NaverOAuthService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class OAuthServiceFactoryTest {

  private final List<OAuthService> services =
      List.of(
          new GoogleOAuthService(null, null, null),
          new KakaoOAuthService(null, null, null),
          new NaverOAuthService(null, null, null));

  private final OAuthServiceFactory factory = new OAuthServiceFactory(services);

  @Nested
  @DisplayName("getOAuthService")
  class GetOAuthService {

    @Test
    @DisplayName("GOOGLE provider로 GoogleOAuthService를 반환한다")
    void GOOGLE_provider로_GoogleOAuthService를_반환한다() {
      OAuthService service = factory.getOAuthService(OauthProvider.GOOGLE);
      assertThat(service).isInstanceOf(GoogleOAuthService.class);
    }

    @Test
    @DisplayName("KAKAO provider로 KakaoOAuthService를 반환한다")
    void KAKAO_provider로_KakaoOAuthService를_반환한다() {
      OAuthService service = factory.getOAuthService(OauthProvider.KAKAO);
      assertThat(service).isInstanceOf(KakaoOAuthService.class);
    }

    @Test
    @DisplayName("NAVER provider로 NaverOAuthService를 반환한다")
    void NAVER_provider로_NaverOAuthService를_반환한다() {
      OAuthService service = factory.getOAuthService(OauthProvider.NAVER);
      assertThat(service).isInstanceOf(NaverOAuthService.class);
    }

    @Test
    @DisplayName("LOCAL provider는 등록된 서비스가 없어 예외가 발생한다")
    void LOCAL_provider는_예외가_발생한다() {
      assertThatThrownBy(() -> factory.getOAuthService(OauthProvider.LOCAL))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.OAUTH_UNSUPPORTED_PROVIDER);
    }
  }
}