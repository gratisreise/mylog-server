package com.mylog.service;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.notification.NotificationResponse;
import com.mylog.model.entity.Member;
import com.mylog.model.entity.Notification;
import com.mylog.model.entity.NotificationSetting;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.NotificationRepository;
import com.mylog.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberReadService memberReadService;

    @Transactional
    public void sendNotification(Member member, Long relatedId, String type) {
        //알림이 켜져 있는지 확인
         if(notificationIsDisabled(member, type)) return;

        //알림생성
        Notification notification = new Notification(member, relatedId, type);

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

    //알람받기
    public Page<NotificationResponse> receiveNotification(CustomUser customUser, Pageable pageable){
        Member member = memberReadService.getByCustomUser(customUser);

        return notificationRepository
            .findAllByMemberAndReadFalse(member, pageable)
            .map(NotificationResponse::from);
    };

    //알림끄기
    @Transactional
    public void toggleNotification(CustomUser customUser, String type){
        Member member = memberReadService.getByCustomUser(customUser);
        notificationSettingRepository
            .findByMemberAndType(member, type)
            .orElseThrow(CMissingDataException::new)
            .toggle();
    };

}
