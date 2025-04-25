package com.mylog.dto.notification;

import com.mylog.entity.Comment;
import com.mylog.entity.Notification;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

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
