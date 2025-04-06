package com.mylog.dto.notification;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Getter
@Service
public class NotificationResponse {
    private String notificationId;
    private String articleId;
    private String message;
}
