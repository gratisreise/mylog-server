package com.mylog.api.notification.service;

import com.mylog.api.member.entity.Member;
import com.mylog.api.notification.entity.Notification;
import com.mylog.api.notification.repository.NotificationRepository;
import com.mylog.api.notificationsetting.service.NotificationSettingReader;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationWriter {
    private final NotificationRepository notificationRepository;
    private final NotificationReader notificationReader;
    private final NotificationSettingReader notificationSettingReader;

    @Async
    public void sendNotification(Member member, Long relatedId, String type) {
        //알림 ON 확인
         if(notificationSettingReader.isDisabled(member, type)) return;

        //알림생성
        Notification notification = Notification.builder()
            .member(member)
            .relatedId(relatedId)
            .type(type)
            .build();

        notificationRepository.save(notification);
    }

    public void readNotification(Long notificationId){
        notificationReader.getById(notificationId).read();
    };

}
