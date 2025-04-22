package com.mylog.service.notification;

import com.mylog.entity.Member;
import com.mylog.entity.Notification;
import com.mylog.entity.NotificationSetting;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.NotificationRepository;
import com.mylog.repository.NotificationSettingRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommonNotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;

    @Transactional
    public void sendNotification(Member member, Long relatedId, String type) {
        //알림이 켜져 있는지 확인
         if(notificationIsDisabled(member, type)) return;

        //알림생성
        Notification notification = Notification.builder()
            .member(member)
            .type(type)
            .relatedId(relatedId)
            .build();

        notificationRepository.save(notification);
    }

    private boolean notificationIsDisabled(Member member, String type) {
        return notificationSettingRepository.findByMemberAndType(member, type)
            .orElseThrow(CMissingDataException::new)
            .isDisabled();
    }

    @Transactional
    public void readNotification(Long notificationId){
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(CMissingDataException::new);
        notification.makeRead();
    };

    @Transactional
    public void createNotificationSetting(Member member, String type){
        if(notificationSettingRepository.existsByMemberAndType(member, type)){
            return;
        }

        NotificationSetting setting = NotificationSetting.builder()
            .member(member)
            .type(type)
            .build();

        notificationSettingRepository.save(setting);
    }


}
