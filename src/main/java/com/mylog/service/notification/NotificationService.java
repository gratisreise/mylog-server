package com.mylog.service.notification;

import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Member;
import com.mylog.model.entity.Notification;
import com.mylog.model.entity.NotificationSetting;
import com.mylog.repository.notification.NotificationRepository;
import com.mylog.service.member.MemberReader;
import com.mylog.service.notificationsetting.NotificationSettingReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationReader notificationReader;
    private final NotificationSettingReader notificationSettingReader;
    private final MemberReader memberReader;

    @Transactional
    public void sendNotification(Member member, Long relatedId, String type) {
        //알림 ON 확인
         if(notificationSettingReader.isDisabled(member, type)) return;

        //알림생성
        Notification notification = new Notification(member, relatedId, type);

        notificationRepository.save(notification);
    }


    @Transactional
    public void readNotification(Long notificationId){
        notificationReader.getById(notificationId).read();
    };

}
