package com.mylog.notification.dto;


import com.mylog.notification.entity.Notification;
import lombok.Builder;

@Builder
public record NotificationResponse(
    String message,
    Long notificationId,
    Long articleId,
    String type
) {
    public static NotificationResponse from(Notification notification){
        return NotificationResponse.builder()
            .message("새로운 대글이 작성되었습니다.")
            .notificationId(notification.getId())
            .articleId(notification.getRelatedId())
            .type(notification.getType())
            .build();
    }
}
