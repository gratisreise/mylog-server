package com.mylog.domain.member.service;

import static org.mockito.BDDMockito.*;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.entity.NotificationSetting;
import com.mylog.domain.member.repository.NotificationSettingRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationSettingWriter 단위 테스트")
class NotificationSettingWriterTest {

  @Mock private NotificationSettingRepository notificationSettingRepository;

  @InjectMocks private NotificationSettingWriter notificationSettingWriter;

  private static final Long MEMBER_ID = 1L;
  private static final String NOTIFICATION_TYPE = "COMMENT";

  @Nested
  @DisplayName("createNotificationSetting 메서드")
  class CreateNotificationSetting {

    @Test
    @DisplayName("성공: 새로운 알림 설정 생성")
    void createNotificationSetting_Success() {
      // given
      Member member = createMember();
      given(notificationSettingRepository.existsByMemberAndType(member, NOTIFICATION_TYPE))
          .willReturn(false);
      given(notificationSettingRepository.save(any(NotificationSetting.class)))
          .willAnswer(invocation -> invocation.getArgument(0));

      // when
      notificationSettingWriter.createNotificationSetting(member, NOTIFICATION_TYPE);

      // then
      then(notificationSettingRepository).should().existsByMemberAndType(member, NOTIFICATION_TYPE);
      then(notificationSettingRepository).should().save(any(NotificationSetting.class));
    }

    @Test
    @DisplayName("성공: 이미 존재하는 알림 설정이면 스킵")
    void createNotificationSetting_SkipIfExists() {
      // given
      Member member = createMember();
      given(notificationSettingRepository.existsByMemberAndType(member, NOTIFICATION_TYPE))
          .willReturn(true);

      // when
      notificationSettingWriter.createNotificationSetting(member, NOTIFICATION_TYPE);

      // then
      then(notificationSettingRepository).should().existsByMemberAndType(member, NOTIFICATION_TYPE);
      then(notificationSettingRepository).should(never()).save(any());
    }
  }

  @Nested
  @DisplayName("toggleNotification 메서드")
  class ToggleNotification {

    @Test
    @DisplayName("성공: 알림 설정 토글")
    void toggleNotification_Success() {
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
      notificationSettingWriter.toggleNotification(MEMBER_ID, NOTIFICATION_TYPE);

      // then
      then(notificationSettingRepository).should().findByMemberIdAndType(MEMBER_ID, NOTIFICATION_TYPE);
    }

    @Test
    @DisplayName("실패: 알림 설정이 없으면 MEMBER_NOT_FOUND 예외")
    void toggleNotification_NotFound() {
      // given
      given(notificationSettingRepository.findByMemberIdAndType(MEMBER_ID, NOTIFICATION_TYPE))
          .willReturn(Optional.empty());

      // when & then
      org.assertj.core.api.Assertions.assertThatThrownBy(
              () -> notificationSettingWriter.toggleNotification(MEMBER_ID, NOTIFICATION_TYPE))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.MEMBER_NOT_FOUND);

      then(notificationSettingRepository).should().findByMemberIdAndType(MEMBER_ID, NOTIFICATION_TYPE);
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
