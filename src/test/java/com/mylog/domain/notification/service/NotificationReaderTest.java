package com.mylog.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.notification.Notification;
import com.mylog.domain.notification.dto.NotificationResponse;
import com.mylog.domain.notification.repository.NotificationRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class NotificationReaderTest {

  private static final Long MEMBER_ID = 1L;
  private static final Long NOTIFICATION_ID = 100L;
  private static final Long RELATED_ID = 200L;
  private static final String TYPE = "COMMENT";

  @Mock private NotificationRepository notificationRepository;
  @Mock private MemberReader memberReader;

  @InjectMocks private NotificationReader notificationReader;

  @Nested
  @DisplayName("getById 테스트")
  class GetById {

    @Test
    @DisplayName("알림 ID로 알림 조회 성공")
    void getById_Success() {
      // given
      Member member = mock(Member.class);
      Notification notification =
          Notification.builder()
              .id(NOTIFICATION_ID)
              .member(member)
              .relatedId(RELATED_ID)
              .type(TYPE)
              .build();

      given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.of(notification));

      // when
      Notification result = notificationReader.getById(NOTIFICATION_ID);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(NOTIFICATION_ID);
      assertThat(result.getType()).isEqualTo(TYPE);
      assertThat(result.getRelatedId()).isEqualTo(RELATED_ID);
    }

    @Test
    @DisplayName("존재하지 않는 알림 ID로 조회 시 예외 발생")
    void getById_NotFound_ThrowsException() {
      // given
      given(notificationRepository.findById(NOTIFICATION_ID)).willReturn(Optional.empty());

      // when & then
      assertThatThrownBy(() -> notificationReader.getById(NOTIFICATION_ID))
          .isInstanceOf(BusinessException.class)
          .extracting("code")
          .isEqualTo(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
  }

  @Nested
  @DisplayName("receiveNotification 테스트")
  class ReceiveNotification {

    @Test
    @DisplayName("회원의 알림 목록 조회 성공")
    void receiveNotification_Success() {
      // given
      Member member = mock(Member.class);
      Notification notification =
          Notification.builder()
              .id(NOTIFICATION_ID)
              .member(member)
              .relatedId(RELATED_ID)
              .type(TYPE)
              .build();

      Pageable pageable = PageRequest.of(0, 10);
      Page<Notification> notificationPage = new PageImpl<>(List.of(notification), pageable, 1);

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(notificationRepository.findByMemberAndReadTrue(member, pageable))
          .willReturn(notificationPage);

      // when
      Page<NotificationResponse> result =
          notificationReader.receiveNotification(MEMBER_ID, pageable);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).notificationId()).isEqualTo(NOTIFICATION_ID);
      assertThat(result.getContent().get(0).articleId()).isEqualTo(RELATED_ID);
      assertThat(result.getContent().get(0).type()).isEqualTo(TYPE);
    }

    @Test
    @DisplayName("알림이 없는 경우 빈 페이지 반환")
    void receiveNotification_EmptyPage() {
      // given
      Member member = mock(Member.class);
      Pageable pageable = PageRequest.of(0, 10);
      Page<Notification> emptyPage = new PageImpl<>(List.of(), pageable, 0);

      given(memberReader.getById(MEMBER_ID)).willReturn(member);
      given(notificationRepository.findByMemberAndReadTrue(member, pageable)).willReturn(emptyPage);

      // when
      Page<NotificationResponse> result =
          notificationReader.receiveNotification(MEMBER_ID, pageable);

      // then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).isEmpty();
      assertThat(result.getTotalElements()).isEqualTo(0);
    }
  }
}
