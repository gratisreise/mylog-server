package com.mylog.api.notification.dto;


import com.mylog.notification.entity.Notification;

public record NotificationResponse(String message, Long notificationId, Long articleId, String type) {

    public static NotificationResponse from(Notification notification){
        return new NotificationResponse(
            "새로운 댓글일 작성되었습니다.",
            notification.getId(),
            notification.getRelatedId(),
            notification.getType()
        );
    }
}
