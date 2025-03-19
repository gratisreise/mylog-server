package com.mylog.service;


import com.mylog.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {
    private final NotificationSettingRepository notificationSettingRepository;
}
