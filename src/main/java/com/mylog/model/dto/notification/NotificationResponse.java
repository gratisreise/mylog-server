package com.mylog.model.dto.notification;

import com.mylog.model.entity.Notification;


public record NotificationResponse(Long notificationId, Long articleId, String type) {

    public  NotificationResponse(Notification notification) {
        this(notification.getId(), notification.getRelatedId(), notification.getType());
    }
}
