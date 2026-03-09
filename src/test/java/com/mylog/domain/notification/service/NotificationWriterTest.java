package com.mylog.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.NotificationSettingReader;
import com.mylog.domain.notification.Notification;
import com.mylog.domain.notification.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationWriterTest {

  private static final Long MEMBER_ID = 1L;
  private static final Long NOTIFICATION_ID = 100L;
  private static final Long RELATED_ID = 200L;
  private static final String TYPE = "COMMENT";

  @Mock private NotificationRepository notificationRepository;
  @Mock private NotificationReader notificationReader;
  @Mock private NotificationSettingReader notificationSettingReader;

  @InjectMocks private NotificationWriter notificationWriter;

  @Nested
  @DisplayName("sendNotification 테스트")
  class SendNotification {

    @Test
    @DisplayName("알림 생성 성공")
    void sendNotification_Success() {
      // given
      Member member = mock(Member.class);
      given(notificationSettingReader.isDisabled(member, TYPE)).willReturn(false);

      // when
      notificationWriter.sendNotification(member, RELATED_ID, TYPE);

      // then
      then(notificationRepository).should().save(any(Notification.class));
    }

    @Test
    @DisplayName("알림 설정이 비활성화된 경우 알림 생성하지 않음")
    void sendNotification_Disabled_DoesNotSave() {
      // given
      Member member = mock(Member.class);
      given(notificationSettingReader.isDisabled(member, TYPE)).willReturn(true);

      // when
      notificationWriter.sendNotification(member, RELATED_ID, TYPE);

      // then
      then(notificationRepository).should(never()).save(any(Notification.class));
    }
  }

  @Nested
  @DisplayName("readNotification 테스트")
  class ReadNotification {

    @Test
    @DisplayName("알림 읽음 처리 성공")
    void readNotification_Success() {
      // given
      Member member = mock(Member.class);
      given(member.getId()).willReturn(MEMBER_ID);

      Notification notification = mock(Notification.class);
      given(notification.getMember()).willReturn(member);
      given(notificationReader.getById(NOTIFICATION_ID)).willReturn(notification);

      // when
      notificationWriter.readNotification(MEMBER_ID, NOTIFICATION_ID);

      // then
      then(notification).should().read();
    }

    @Test
    @DisplayName("다른 사용자의 알림 읽음 처리 시 예외 발생")
    void readNotification_AccessDenied_ThrowsException() {
      // given
      Long otherMemberId = 999L;

      Member member = mock(Member.class);
      given(member.getId()).willReturn(MEMBER_ID);

      Notification notification = mock(Notification.class);
      given(notification.getMember()).willReturn(member);
      given(notificationReader.getById(NOTIFICATION_ID)).willReturn(notification);

      // when & then
      assertThatThrownBy(() -> notificationWriter.readNotification(otherMemberId, NOTIFICATION_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.ACCESS_DENIED);

      then(notification).should(never()).read();
    }
  }
}
