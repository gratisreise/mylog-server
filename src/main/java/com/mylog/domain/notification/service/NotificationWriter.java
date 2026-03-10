package com.mylog.domain.notification.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.NotificationSettingReader;
import com.mylog.domain.notification.Notification;
import com.mylog.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationWriter {
  private final NotificationRepository notificationRepository;
  private final NotificationReader notificationReader;
  private final NotificationSettingReader notificationSettingReader;

  @Async("threadPoolTaskExecutor")
  public void sendNotification(Member member, Long relatedId, String type) {
    // 알림 ON 확인
    if (notificationSettingReader.isDisabled(member, type)) return;

    // 알림생성
    Notification notification =
        Notification.builder().member(member).relatedId(relatedId).type(type).build();

    notificationRepository.save(notification);
  }

  // 알림 읽음 처리
  public void readNotification(Long memberId, Long notificationId) {
    Notification notification = notificationReader.getById(notificationId);

    // 소유권 검증
    if (!notification.getMember().getId().equals(memberId)) {
      throw new BusinessException(ErrorCode.ACCESS_DENIED);
    }

    notification.read();
  }
}
