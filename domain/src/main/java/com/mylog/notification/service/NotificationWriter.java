package com.mylog.notification.service;

import com.mylog.member.entity.Member;
import com.mylog.notification.entity.Notification;
import com.mylog.notification.repository.NotificationRepository;

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

    public void readNotification(Long notificationId){
        notificationReader.getById(notificationId).read();
    };

//    @Async
//    public void sendNotification(Member member, Long relatedId, String type) {
//        //알림 ON 확인
//         if(notificationSettingReader.isDisabled(member, type)) return;
//
//        //알림생성
//        Notification notification = Notification.builder()
//            .member(member)
//            .relatedId(relatedId)
//            .type(type)
//            .build();
//
//        notificationRepository.save(notification);
//    }
//


}
