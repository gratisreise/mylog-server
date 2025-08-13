package com.mylog.service.notification;

import com.mylog.repository.notification.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationReader {
    private final NotificationRepository notificationRepository;

}
