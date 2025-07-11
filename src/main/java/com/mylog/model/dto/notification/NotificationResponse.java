package com.mylog.model.dto.notification;

import com.mylog.model.entity.Notification;


public record NotificationResponse(String message, Long notificationId, Long articleId, String type) {

    public  NotificationResponse(Notification notification) {
        this( "새로운 댓글이 작성되었습니다.",
            notification.getId(),
            notification.getRelatedId(),
            notification.getType());
    }
}
