package com.mylog.service.notification;

import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.notification.NotificationResponse;
import org.springframework.data.domain.Page;

public interface NotificationService {
    Page<NotificationResponse> receiveNotification(CustomUser customUser);
    void toggleNotification(CustomUser customUser, String type);
}
