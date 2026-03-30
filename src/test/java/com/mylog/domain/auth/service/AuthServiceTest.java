package com.mylog.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.auth.dto.request.LoginRequest;
import com.mylog.domain.auth.dto.request.RefreshRequest;
import com.mylog.domain.auth.dto.request.SignUpRequest;
import com.mylog.domain.auth.dto.response.LoginResponse;
import com.mylog.domain.auth.dto.response.RefreshResponse;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.member.service.MemberWriter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final Long MEMBER_ID = 1L;
  private static final String EMAIL = "test@example.com";
  private static final String PASSWORD = "Password123!";
  private static final String MEMBER_NAME = "테스트유저";
  private static final String ACCESS_TOKEN = "accessToken";
  private static final String REFRESH_TOKEN = "refreshToken";
  private static final String AUTH_HEADER = "Bearer " + ACCESS_TOKEN;

  @Mock private MemberReader memberReader;
  @Mock private TokenService tokenService;
  @Mock private PasswordEncoder encoder;
  @Mock private MemberWriter memberWriter;

  @InjectMocks private AuthService authService;

  @Nested
  @DisplayName("회원가입")
  class SignUp {

    @Test
    @DisplayName("정상 회원가입에 성공한다")
    void 정상_회원가입에_성공한다() {
      // given
      SignUpRequest request = new SignUpRequest(EMAIL, MEMBER_NAME, PASSWORD);
      given(memberReader.existsByEmail(EMAIL)).willReturn(false);
      given(encoder.encode(PASSWORD)).willReturn("encodedPassword");
      willDoNothing().given(memberWriter).save(any(Member.class));

      // when
      authService.signUp(request);

      // then
      then(memberReader).should().existsByEmail(EMAIL);
      then(memberWriter).should().save(any(Member.class));
    }

    @Test
    @DisplayName("이메일 중복 시 예외가 발생한다")
    void 이메일_중복_시_예외가_발생한다() {
      // given
      SignUpRequest request = new SignUpRequest(EMAIL, MEMBER_NAME, PASSWORD);
      given(memberReader.existsByEmail(EMAIL)).willReturn(true);

      // when & then
      assertThatThrownBy(() -> authService.signUp(request))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.MEMBER_EMAIL_ALREADY_EXISTS);

      then(memberWriter).shouldHaveNoInteractions();
    }
  }

  @Nested
  @DisplayName("로그인")
  class Login {

    @Test
    @DisplayName("정상 로그인에 성공한다")
    void 정상_로그인에_성공한다() {
      // given
      LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
      Member member = createMember();
      LoginResponse expectedResponse = LoginResponse.of(ACCESS_TOKEN, REFRESH_TOKEN);

      given(memberReader.getByEmail(EMAIL)).willReturn(member);
      given(encoder.matches(PASSWORD, member.getPassword())).willReturn(true);
      given(tokenService.generateToken(member.getId())).willReturn(expectedResponse);

      // when
      LoginResponse response = authService.login(request);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
      assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);

      then(memberReader).should().getByEmail(EMAIL);
      then(tokenService).should().generateToken(member.getId());
    }

    @Test
    @DisplayName("존재하지 않는 이메일로 로그인 시 예외가 발생한다")
    void 존재하지_않는_이메일로_로그인_시_예외가_발생한다() {
      // given
      LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
      willThrow(new BusinessException(ErrorCode.MEMBER_NOT_FOUND))
          .given(memberReader)
          .getByEmail(EMAIL);

      // when & then
      assertThatThrownBy(() -> authService.login(request))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

      then(tokenService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("비밀번호 불일치 시 예외가 발생한다")
    void 비밀번호_불일치_시_예외가_발생한다() {
      // given
      LoginRequest request = new LoginRequest(EMAIL, PASSWORD);
      Member member = createMember();

      given(memberReader.getByEmail(EMAIL)).willReturn(member);
      given(encoder.matches(PASSWORD, member.getPassword())).willReturn(false);

      // when & then
      assertThatThrownBy(() -> authService.login(request))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.INVALID_CREDENTIALS);

      then(tokenService).shouldHaveNoInteractions();
    }
  }

  @Nested
  @DisplayName("토큰 재발급")
  class Refresh {

    @Test
    @DisplayName("정상 토큰 재발급에 성공한다")
    void 정상_토큰_재발급에_성공한다() {
      // given
      RefreshRequest request = new RefreshRequest(REFRESH_TOKEN, null);
      RefreshResponse expectedResponse = RefreshResponse.of(ACCESS_TOKEN, REFRESH_TOKEN);

      given(tokenService.reissueToken(REFRESH_TOKEN)).willReturn(expectedResponse);

      // when
      RefreshResponse response = authService.refresh(request);

      // then
      assertThat(response).isNotNull();
      assertThat(response.getAccessToken()).isEqualTo(ACCESS_TOKEN);
      assertThat(response.getRefreshToken()).isEqualTo(REFRESH_TOKEN);

      then(tokenService).should().reissueToken(REFRESH_TOKEN);
    }
  }

  @Nested
  @DisplayName("로그아웃")
  class Logout {

    @Test
    @DisplayName("정상 로그아웃에 성공한다")
    void 정상_로그아웃에_성공한다() {
      // given
      willDoNothing().given(tokenService).logout(AUTH_HEADER, MEMBER_ID);

      // when
      authService.logout(AUTH_HEADER, MEMBER_ID);

      // then
      then(tokenService).should().logout(AUTH_HEADER, MEMBER_ID);
    }
  }

  private Member createMember() {
    return Member.builder()
        .id(MEMBER_ID)
        .email(EMAIL)
        .password("encodedPassword")
        .memberName(MEMBER_NAME)
        .build();
  }
}