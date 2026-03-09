package com.mylog.domain.member.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.dto.NotificationSettingResponse;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.entity.NotificationSetting;
import com.mylog.domain.member.repository.NotificationSettingRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationSettingReader 단위 테스트")
class NotificationSettingReaderTest {

  @Mock private NotificationSettingRepository notificationSettingRepository;

  @InjectMocks private NotificationSettingReader notificationSettingReader;

  private static final Long MEMBER_ID = 1L;
  private static final String NOTIFICATION_TYPE = "COMMENT";

  @Nested
  @DisplayName("isDisabled 메서드")
  class IsDisabled {

    @Test
    @DisplayName("성공: 알림이 비활성화되어 있으면 true 반환")
    void isDisabled_True() {
      // given
      Member member = createMember();
      NotificationSetting setting = NotificationSetting.builder()
          .member(member)
          .type(NOTIFICATION_TYPE)
          .disabled(true)
          .build();

      given(notificationSettingRepository.findByMemberIdAndType(MEMBER_ID, NOTIFICATION_TYPE))
          .willReturn(Optional.of(setting));

      // when
      boolean result = notificationSettingReader.isDisabled(member, NOTIFICATION_TYPE);

      // then
      assertThat(result).isTrue();
      then(notificationSettingRepository).should().findByMemberIdAndType(MEMBER_ID, NOTIFICATION_TYPE);
    }

    @Test
    @DisplayName("성공: 알림이 활성화되어 있으면 false 반환")
    void isDisabled_False() {
      // given
      Member member = createMember();
      NotificationSetting setting = NotificationSetting.builder()
          .member(member)
          .type(NOTIFICATION_TYPE)
          .disabled(false)
          .build();

      given(notificationSettingRepository.findByMemberIdAndType(MEMBER_ID, NOTIFICATION_TYPE))
          .willReturn(Optional.of(setting));

      // when
      boolean result = notificationSettingReader.isDisabled(member, NOTIFICATION_TYPE);

      // then
      assertThat(result).isFalse();
      then(notificationSettingRepository).should().findByMemberIdAndType(MEMBER_ID, NOTIFICATION_TYPE);
    }

    @Test
    @DisplayName("실패: 알림 설정이 없으면 NOTIFICATION_NOT_FOUND 예외")
    void isDisabled_NotFound() {
      // given
      Member member = createMember();
      given(notificationSettingRepository.findByMemberIdAndType(MEMBER_ID, NOTIFICATION_TYPE))
          .willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> notificationSettingReader.isDisabled(member, NOTIFICATION_TYPE))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);

      then(notificationSettingRepository).should().findByMemberIdAndType(MEMBER_ID, NOTIFICATION_TYPE);
    }
  }

  @Nested
  @DisplayName("getNotificationSettings 메서드")
  class GetNotificationSettings {

    @Test
    @DisplayName("성공: 알림 설정 목록 반환")
    void getNotificationSettings_Success() {
      // given
      Member member = createMember();
      NotificationSetting setting1 = NotificationSetting.builder()
          .member(member)
          .type("COMMENT")
          .disabled(false)
          .build();
      NotificationSetting setting2 = NotificationSetting.builder()
          .member(member)
          .type("LIKE")
          .disabled(true)
          .build();

      given(notificationSettingRepository.findByMemberId(MEMBER_ID))
          .willReturn(List.of(setting1, setting2));

      // when
      List<NotificationSettingResponse> result = notificationSettingReader.getNotificationSettings(MEMBER_ID);

      // then
      assertThat(result).hasSize(2);
      assertThat(result.get(0).type()).isEqualTo("COMMENT");
      assertThat(result.get(0).disabled()).isFalse();
      assertThat(result.get(1).type()).isEqualTo("LIKE");
      assertThat(result.get(1).disabled()).isTrue();
      then(notificationSettingRepository).should().findByMemberId(MEMBER_ID);
    }

    @Test
    @DisplayName("성공: 알림 설정이 없으면 빈 목록 반환")
    void getNotificationSettings_EmptyList() {
      // given
      given(notificationSettingRepository.findByMemberId(MEMBER_ID))
          .willReturn(List.of());

      // when
      List<NotificationSettingResponse> result = notificationSettingReader.getNotificationSettings(MEMBER_ID);

      // then
      assertThat(result).isEmpty();
      then(notificationSettingRepository).should().findByMemberId(MEMBER_ID);
    }
  }

  private Member createMember() {
    return Member.builder()
        .id(MEMBER_ID)
        .email("test@example.com")
        .nickname("테스트유저")
        .memberName("테스트")
        .profileImg("https://example.com/profile.jpg")
        .build();
  }
}
