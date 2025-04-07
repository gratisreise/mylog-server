package com.mylog.service.notification;

import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.notification.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    Page<NotificationResponse> receiveNotification(CustomUser customUser, Pageable pageable);
    void toggleNotification(CustomUser customUser, String type);
}
