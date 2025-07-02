package com.mylog.model.dto.notification;

import com.mylog.model.entity.Notification;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class NotificationResponse {
    private Long notificationId;
    private Long articleId;
    private String type;

    public static NotificationResponse from(
        Notification notification
    ) {
        return NotificationResponse.builder()
            .notificationId(notification.getId())
            .articleId(notification.getRelatedId())
            .type(notification.getType())
            .build();
    }
}
