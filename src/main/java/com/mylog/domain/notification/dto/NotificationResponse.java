package com.mylog.domain.notification.dto;

import com.mylog.domain.notification.Notification;
import lombok.Builder;

@Builder
public record NotificationResponse(
    String message, Long notificationId, Long articleId, String type) {

  public static NotificationResponse from(Notification notification) {
    return NotificationResponse.builder()
        .message("새로운 댓글일 작성되었습니다.")
        .notificationId(notification.getId())
        .articleId(notification.getRelatedId())
        .type(notification.getType())
        .build();
  }
}
