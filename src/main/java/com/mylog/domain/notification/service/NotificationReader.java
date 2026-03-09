package com.mylog.domain.notification.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.notification.Notification;
import com.mylog.domain.notification.dto.NotificationResponse;
import com.mylog.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationReader {
  private final NotificationRepository notificationRepository;
  private final MemberReader memberReader;

  public Notification getById(long notificationId) {
    return notificationRepository
        .findById(notificationId)
        .orElseThrow(() -> new BusinessException(ErrorCode.NOTIFICATION_NOT_FOUND));
  }

  // 알림목록조회
  public Page<NotificationResponse> receiveNotification(Long memberId, Pageable pageable) {
    Member member = memberReader.getById(memberId);
    return notificationRepository
        .findByMemberAndReadTrue(member, pageable)
        .map(NotificationResponse::from);
  }
  ;
}
