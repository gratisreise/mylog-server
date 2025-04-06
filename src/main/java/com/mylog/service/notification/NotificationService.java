package com.mylog.service.notification;

import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.notification.NotificationResponse;
import org.springframework.data.domain.Page;

public interface NotificationService {
    void sendNotification(CustomUser customUser, Long RelateId);
    Page<NotificationResponse> receiveNotification(CustomUser customUser);
    void readNotification(Long notificationId);
    void toggleNotification(CustomUser customUser, String type);
}
