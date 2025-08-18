package com.mylog.service.social;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.mylog.enums.OauthProvider;
import com.mylog.exception.CInvalidDataException;
import com.mylog.service.social.google.GoogleOAuth2UserService;
import com.mylog.service.social.kakao.KakaoOAuth2UserService;
import com.mylog.service.social.naver.NaverOAuth2UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * OAuth2UserServiceFactory 단위 테스트
 * OAuth2 제공자별 서비스 팩토리 클래스의 서비스 선택 로직과 예외 처리를 테스트합니다.
 * 지원되는 제공자(Google, Naver, Kakao)에 대한 정확한 서비스 반환과
 * 지원되지 않는 제공자에 대한 예외 발생을 검증합니다.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OAuth2UserServiceFactory 단위 테스트")
public class OAuth2UserServiceFactoryTest {

    @InjectMocks
    private OAuth2UserServiceFactory oAuth2UserServiceFactory;

    @Mock
    private GoogleOAuth2UserService googleOAuth2UserService;

    @Mock
    private NaverOAuth2UserService naverOAuth2UserService;

    @Mock
    private KakaoOAuth2UserService kakaoOAuth2UserService;

    @Nested
    @DisplayName("getOAuth2UserService 메서드 테스트")
    class GetOAuth2UserServiceTests {

        @Test
        @DisplayName("Google 제공자로 GoogleOAuth2UserService를 정상적으로 반환한다")
        void getOAuth2UserService_WithGoogleProvider_ReturnsGoogleService() {
            // When
            OAuth2UserService result = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.GOOGLE);

            // Then
            assertThat(result).isSameAs(googleOAuth2UserService);
            assertThat(result).isInstanceOf(GoogleOAuth2UserService.class);
        }

        @Test
        @DisplayName("Naver 제공자로 NaverOAuth2UserService를 정상적으로 반환한다")
        void getOAuth2UserService_WithNaverProvider_ReturnsNaverService() {
            // When
            OAuth2UserService result = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.NAVER);

            // Then
            assertThat(result).isSameAs(naverOAuth2UserService);
            assertThat(result).isInstanceOf(NaverOAuth2UserService.class);
        }

        @Test
        @DisplayName("Kakao 제공자로 KakaoOAuth2UserService를 정상적으로 반환한다")
        void getOAuth2UserService_WithKakaoProvider_ReturnsKakaoService() {
            // When
            OAuth2UserService result = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.KAKAO);

            // Then
            assertThat(result).isSameAs(kakaoOAuth2UserService);
            assertThat(result).isInstanceOf(KakaoOAuth2UserService.class);
        }

        @Test
        @DisplayName("LOCAL 제공자로 호출시 CInvalidDataException 예외가 발생한다")
        void getOAuth2UserService_WithLocalProvider_ThrowsCInvalidDataException() {
            // When & Then
            assertThatThrownBy(() -> oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.LOCAL))
                .isInstanceOf(CInvalidDataException.class)
                .hasMessage("지원하지 않는 OAuth 제공자입니다." + OauthProvider.LOCAL);
        }

        @Test
        @DisplayName("SOCIAL 제공자로 호출시 CInvalidDataException 예외가 발생한다")
        void getOAuth2UserService_WithSocialProvider_ThrowsCInvalidDataException() {
            // When & Then
            assertThatThrownBy(() -> oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.SOCIAL))
                .isInstanceOf(CInvalidDataException.class)
                .hasMessage("지원하지 않는 OAuth 제공자입니다." + OauthProvider.SOCIAL);
        }

        @Test
        @DisplayName("null 제공자로 호출시 NullPointerException이 발생한다")
        void getOAuth2UserService_WithNullProvider_ThrowsNullPointerException() {
            // When & Then
            assertThatThrownBy(() -> oAuth2UserServiceFactory.getOAuth2UserService(null))
                .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("팩토리 패턴 검증 테스트")
    class FactoryPatternTests {

        @Test
        @DisplayName("모든 지원되는 OAuth 제공자에 대해 서로 다른 서비스 인스턴스를 반환한다")
        void getOAuth2UserService_ForAllSupportedProviders_ReturnsDifferentServices() {
            // When
            OAuth2UserService googleService = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.GOOGLE);
            OAuth2UserService naverService = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.NAVER);
            OAuth2UserService kakaoService = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.KAKAO);

            // Then
            assertThat(googleService).isNotSameAs(naverService);
            assertThat(naverService).isNotSameAs(kakaoService);
            assertThat(kakaoService).isNotSameAs(googleService);
            
            // 각각 올바른 타입인지 확인
            assertThat(googleService).isInstanceOf(GoogleOAuth2UserService.class);
            assertThat(naverService).isInstanceOf(NaverOAuth2UserService.class);
            assertThat(kakaoService).isInstanceOf(KakaoOAuth2UserService.class);
        }

        @Test
        @DisplayName("동일한 제공자로 여러 번 호출해도 같은 서비스 인스턴스를 반환한다")
        void getOAuth2UserService_MultipleCallsWithSameProvider_ReturnsSameInstance() {
            // When
            OAuth2UserService firstCall = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.GOOGLE);
            OAuth2UserService secondCall = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.GOOGLE);
            OAuth2UserService thirdCall = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.GOOGLE);

            // Then
            assertThat(firstCall).isSameAs(secondCall);
            assertThat(secondCall).isSameAs(thirdCall);
            assertThat(firstCall).isSameAs(googleOAuth2UserService);
        }
    }

    @Nested
    @DisplayName("예외 메시지 검증 테스트")
    class ExceptionMessageTests {

        @Test
        @DisplayName("지원되지 않는 모든 제공자에 대해 명확한 예외 메시지를 포함한다")
        void getOAuth2UserService_WithUnsupportedProviders_ContainsProviderInMessage() {
            // LOCAL 제공자 테스트
            assertThatThrownBy(() -> oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.LOCAL))
                .isInstanceOf(CInvalidDataException.class)
                .hasMessage("지원하지 않는 OAuth 제공자입니다." + OauthProvider.LOCAL)
                .hasMessageContaining(OauthProvider.LOCAL.toString());

            // SOCIAL 제공자 테스트  
            assertThatThrownBy(() -> oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.SOCIAL))
                .isInstanceOf(CInvalidDataException.class)
                .hasMessage("지원하지 않는 OAuth 제공자입니다." + OauthProvider.SOCIAL)
                .hasMessageContaining(OauthProvider.SOCIAL.toString());
        }

    }

    @Nested
    @DisplayName("타입 안전성 검증 테스트")
    class TypeSafetyTests {

        @Test
        @DisplayName("반환된 모든 서비스가 OAuth2UserService 인터페이스를 구현한다")
        void getOAuth2UserService_AllReturnedServices_ImplementOAuth2UserService() {
            // When
            OAuth2UserService googleService = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.GOOGLE);
            OAuth2UserService naverService = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.NAVER);
            OAuth2UserService kakaoService = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.KAKAO);

            // Then
            assertThat(googleService).isInstanceOf(OAuth2UserService.class);
            assertThat(naverService).isInstanceOf(OAuth2UserService.class);
            assertThat(kakaoService).isInstanceOf(OAuth2UserService.class);
        }

        @Test
        @DisplayName("각 제공자별로 정확한 구현체 타입을 반환한다")
        void getOAuth2UserService_EachProvider_ReturnsCorrectImplementationType() {
            // Google 제공자 검증
            OAuth2UserService googleService = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.GOOGLE);
            assertThat(googleService.getClass().getSimpleName()).isEqualTo("GoogleOAuth2UserService");

            // Naver 제공자 검증
            OAuth2UserService naverService = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.NAVER);
            assertThat(naverService.getClass().getSimpleName()).isEqualTo("NaverOAuth2UserService");

            // Kakao 제공자 검증
            OAuth2UserService kakaoService = oAuth2UserServiceFactory.getOAuth2UserService(OauthProvider.KAKAO);
            assertThat(kakaoService.getClass().getSimpleName()).isEqualTo("KakaoOAuth2UserService");
        }
    }
}