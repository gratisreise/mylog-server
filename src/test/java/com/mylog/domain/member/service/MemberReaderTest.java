package com.mylog.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.repository.MemberRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberReader 단위 테스트")
class MemberReaderTest {

  @Mock private MemberRepository memberRepository;

  @InjectMocks private MemberReader memberReader;

  private static final Long MEMBER_ID = 1L;
  private static final String EMAIL = "test@example.com";
  private static final String NICKNAME = "테스트유저";
  private static final String PROVIDER_ID = "google-12345";

  @Nested
  @DisplayName("getById 메서드")
  class GetById {

    @Test
    @DisplayName("성공: 회원 ID로 조회")
    void getById_Success() {
      // given
      Member member = createMember();
      given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.of(member));

      // when
      Member result = memberReader.getById(MEMBER_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(MEMBER_ID);
      then(memberRepository).should().findById(MEMBER_ID);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 회원이면 MEMBER_NOT_FOUND 예외")
    void getById_NotFound() {
      // given
      given(memberRepository.findById(MEMBER_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> memberReader.getById(MEMBER_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

      then(memberRepository).should().findById(MEMBER_ID);
    }
  }

  @Nested
  @DisplayName("getByNickname 메서드")
  class GetByNickname {

    @Test
    @DisplayName("성공: 닉네임으로 회원 조회")
    void getByNickname_Success() {
      // given
      Member member = createMember();
      given(memberRepository.findByNickname(NICKNAME)).willReturn(Optional.of(member));

      // when
      Member result = memberReader.getByNickname(NICKNAME);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getNickname()).isEqualTo(NICKNAME);
      then(memberRepository).should().findByNickname(NICKNAME);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 닉네임이면 MEMBER_NOT_FOUND 예외")
    void getByNickname_NotFound() {
      // given
      given(memberRepository.findByNickname(NICKNAME)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> memberReader.getByNickname(NICKNAME))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

      then(memberRepository).should().findByNickname(NICKNAME);
    }
  }

  @Nested
  @DisplayName("getByEmail 메서드")
  class GetByEmail {

    @Test
    @DisplayName("성공: 이메일로 회원 조회")
    void getByEmail_Success() {
      // given
      Member member = createMember();
      given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.of(member));

      // when
      Member result = memberReader.getByEmail(EMAIL);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getEmail()).isEqualTo(EMAIL);
      then(memberRepository).should().findByEmail(EMAIL);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 이메일이면 MEMBER_NOT_FOUND 예외")
    void getByEmail_NotFound() {
      // given
      given(memberRepository.findByEmail(EMAIL)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> memberReader.getByEmail(EMAIL))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

      then(memberRepository).should().findByEmail(EMAIL);
    }
  }

  @Nested
  @DisplayName("existsByProviderId 메서드")
  class ExistsByProviderId {

    @Test
    @DisplayName("성공: 존재하는 providerId면 true 반환")
    void existsByProviderId_True() {
      // given
      given(memberRepository.existsByProviderId(PROVIDER_ID)).willReturn(true);

      // when
      boolean result = memberReader.existsByProviderId(PROVIDER_ID);

      // then
      assertThat(result).isTrue();
      then(memberRepository).should().existsByProviderId(PROVIDER_ID);
    }

    @Test
    @DisplayName("성공: 존재하지 않는 providerId면 false 반환")
    void existsByProviderId_False() {
      // given
      given(memberRepository.existsByProviderId(PROVIDER_ID)).willReturn(false);

      // when
      boolean result = memberReader.existsByProviderId(PROVIDER_ID);

      // then
      assertThat(result).isFalse();
      then(memberRepository).should().existsByProviderId(PROVIDER_ID);
    }
  }

  @Nested
  @DisplayName("existsByEmail 메서드")
  class ExistsByEmail {

    @Test
    @DisplayName("성공: 존재하는 이메일이면 true 반환")
    void existsByEmail_True() {
      // given
      given(memberRepository.existsByEmail(EMAIL)).willReturn(true);

      // when
      boolean result = memberReader.existsByEmail(EMAIL);

      // then
      assertThat(result).isTrue();
      then(memberRepository).should().existsByEmail(EMAIL);
    }

    @Test
    @DisplayName("성공: 존재하지 않는 이메일이면 false 반환")
    void existsByEmail_False() {
      // given
      given(memberRepository.existsByEmail(EMAIL)).willReturn(false);

      // when
      boolean result = memberReader.existsByEmail(EMAIL);

      // then
      assertThat(result).isFalse();
      then(memberRepository).should().existsByEmail(EMAIL);
    }
  }

  private Member createMember() {
    return Member.builder()
        .id(MEMBER_ID)
        .email(EMAIL)
        .nickname(NICKNAME)
        .memberName("테스트")
        .profileImg("https://example.com/profile.jpg")
        .build();
  }
}
