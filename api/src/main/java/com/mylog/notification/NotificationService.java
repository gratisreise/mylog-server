package com.mylog.notification;

import com.mylog.auth.CustomUser;
import com.mylog.common.PageResponse;
import com.mylog.notification.dto.NotificationResponse;
import com.mylog.notification.dto.NotificationSettingResponse;
import com.mylog.notification.entity.Notification;
import com.mylog.notification.service.NotificationReader;
import com.mylog.notification.service.NotificationSettingReader;
import com.mylog.notification.service.NotificationSettingWriter;
import com.mylog.notification.service.NotificationWriter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {
    private final NotificationReader notificationReader;
    private final NotificationWriter notificationWriter;
    private final NotificationSettingReader notificationSettingReader;
    private final NotificationSettingWriter notificationSettingWriter;


    public PageResponse<NotificationResponse> receiveNotification(CustomUser customUser, Pageable pageable) {
        Long memberId = customUser.getMemberId();
        Page<NotificationResponse> page =
            notificationReader.receiveNotification(memberId, pageable)
                .map(NotificationResponse::from);
        return PageResponse.from(page);
    }

    @Transactional
    public void readNotification(Long notificationId) {
        notificationWriter.readNotification(notificationId);
    }


    public List<NotificationSettingResponse> getNotificationSettings(CustomUser customUser) {
        Long memberId = customUser.getMemberId();
        return notificationSettingReader.getNotificationSettings(memberId)
            .stream().map(NotificationSettingResponse::from).toList();
    }
}
