package com.mylog.domain.member;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.mylog.domain.member.dto.MemberResponse;
import com.mylog.domain.member.dto.UpdateMemberRequest;
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

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberService 단위 테스트")
class MemberServiceTest {

  @Mock private MemberReader memberReader;
  @Mock private MemberWriter memberWriter;

  @InjectMocks private MemberService memberService;

  private static final Long MEMBER_ID = 1L;
  private static final String NICKNAME = "테스트유저";
  private static final String BIO = "자기소개입니다";
  private static final String IMAGE_URL = "https://example.com/profile.jpg";

  @Nested
  @DisplayName("getMember 메서드")
  class GetMember {

    @Test
    @DisplayName("성공: 회원 조회")
    void getMember_Success() {
      // given
      Member member = createMember();
      given(memberReader.getById(MEMBER_ID)).willReturn(member);

      // when
      MemberResponse result = memberService.getMember(MEMBER_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.id()).isEqualTo(MEMBER_ID);
      assertThat(result.nickname()).isEqualTo(NICKNAME);
      then(memberReader).should().getById(MEMBER_ID);
    }
  }

  @Nested
  @DisplayName("updateMember 메서드")
  class UpdateMember {

    @Test
    @DisplayName("성공: 프로필 수정")
    void updateMember_Success() {
      // given
      Member member = createMember();
      UpdateMemberRequest request =
          new UpdateMemberRequest("password1!", "테스트", NICKNAME, BIO, IMAGE_URL);

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      willDoNothing().given(memberWriter).update(member, request, IMAGE_URL);

      // when
      memberService.updateMember(request, IMAGE_URL, MEMBER_ID);

      // then
      then(memberReader).should().getById(MEMBER_ID);
      then(memberWriter).should().update(member, request, IMAGE_URL);
    }
  }

  @Nested
  @DisplayName("delete 메서드")
  class Delete {

    @Test
    @DisplayName("성공: 회원 삭제")
    void delete_Success() {
      // given
      willDoNothing().given(memberWriter).deleteById(MEMBER_ID);

      // when
      memberService.delete(MEMBER_ID);

      // then
      then(memberWriter).should().deleteById(MEMBER_ID);
    }
  }

  private Member createMember() {
    return Member.builder()
        .id(MEMBER_ID)
        .email("test@example.com")
        .nickname(NICKNAME)
        .memberName("테스트")
        .profileImg(IMAGE_URL)
        .bio(BIO)
        .build();
  }
}
