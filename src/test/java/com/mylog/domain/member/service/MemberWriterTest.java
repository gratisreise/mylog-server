package com.mylog.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.mylog.common.enums.OauthProvider;
import com.mylog.domain.member.dto.UpdateMemberRequest;
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
@DisplayName("MemberWriter 단위 테스트")
class MemberWriterTest {

  @Mock private MemberRepository memberRepository;

  @InjectMocks private MemberWriter memberWriter;

  private static final Long MEMBER_ID = 1L;
  private static final String NICKNAME = "테스트유저";
  private static final String BIO = "자기소개입니다";
  private static final String IMAGE_URL = "https://example.com/profile.jpg";

  @Nested
  @DisplayName("update 메서드")
  class Update {

    @Test
    @DisplayName("성공: 프로필 업데이트")
    void update_Success() {
      // given
      Member member = createMember();
      UpdateMemberRequest request = new UpdateMemberRequest("password1!", "테스트", NICKNAME, BIO, IMAGE_URL);

      // when
      memberWriter.update(member, request, IMAGE_URL);

      // then
      assertThat(member.getNickname()).isEqualTo(NICKNAME);
      assertThat(member.getBio()).isEqualTo(BIO);
      assertThat(member.getProfileImg()).isEqualTo(IMAGE_URL);
    }
  }

  @Nested
  @DisplayName("deleteById 메서드")
  class DeleteById {

    @Test
    @DisplayName("성공: 회원 삭제")
    void deleteById_Success() {
      // given
      willDoNothing().given(memberRepository).deleteById(MEMBER_ID);

      // when
      memberWriter.deleteById(MEMBER_ID);

      // then
      then(memberRepository).should().deleteById(MEMBER_ID);
    }
  }

  @Nested
  @DisplayName("saveOrUpdate 메서드")
  class SaveOrUpdate {

    @Test
    @DisplayName("성공: 기존 회원이 존재하면 기존 회원 반환")
    void saveOrUpdate_ExistingMember() {
      // given
      Member existingMember = createMember();
      Member newMember = Member.builder()
          .provider(OauthProvider.GOOGLE)
          .providerId("google-12345")
          .build();

      given(memberRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, "google-12345"))
          .willReturn(Optional.of(existingMember));

      // when
      Member result = memberWriter.saveOrUpdate(newMember);

      // then
      assertThat(result).isEqualTo(existingMember);
      assertThat(result.getId()).isEqualTo(MEMBER_ID);
      then(memberRepository).should().findByProviderAndProviderId(OauthProvider.GOOGLE, "google-12345");
      then(memberRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("성공: 기존 회원이 없으면 새 회원 저장")
    void saveOrUpdate_NewMember() {
      // given
      Member newMember = Member.builder()
          .provider(OauthProvider.GOOGLE)
          .providerId("google-12345")
          .memberName("새회원")
          .build();

      Member savedMember = Member.builder()
          .id(2L)
          .provider(OauthProvider.GOOGLE)
          .providerId("google-12345")
          .memberName("새회원")
          .build();

      given(memberRepository.findByProviderAndProviderId(OauthProvider.GOOGLE, "google-12345"))
          .willReturn(Optional.empty());
      given(memberRepository.save(newMember)).willReturn(savedMember);

      // when
      Member result = memberWriter.saveOrUpdate(newMember);

      // then
      assertThat(result.getId()).isEqualTo(2L);
      then(memberRepository).should().findByProviderAndProviderId(OauthProvider.GOOGLE, "google-12345");
      then(memberRepository).should().save(newMember);
    }
  }

  private Member createMember() {
    return Member.builder()
        .id(MEMBER_ID)
        .email("test@example.com")
        .nickname(NICKNAME)
        .memberName("테스트")
        .profileImg(IMAGE_URL)
        .bio("기존 자기소개")
        .provider(OauthProvider.GOOGLE)
        .providerId("google-12345")
        .build();
  }
}
