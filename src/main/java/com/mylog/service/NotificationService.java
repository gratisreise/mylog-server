package com.mylog.service;

import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.notification.NotificationResponse;
import com.mylog.entity.Member;
import com.mylog.entity.Notification;
import com.mylog.entity.NotificationSetting;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
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
    private final MemberRepository memberRepository;

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

    //알람받기
    public Page<NotificationResponse> receiveNotification(CustomUser customUser, Pageable pageable){
        Member member = generateMember(customUser);

        return notificationRepository
            .findAllByMemberAndReadFalse(member, pageable)
            .map(NotificationResponse::from);
    };

    //알림끄기
    @Transactional
    public void toggleNotification(CustomUser customUser, String type){
        Member member = generateMember(customUser);
        notificationSettingRepository
            .findByMemberAndType(member, type)
            .orElseThrow(CMissingDataException::new)
            .toggle();
    };

    private Member generateMember(CustomUser customUser) {
        return memberRepository.findById(customUser.getMemberId())
            .orElseThrow(CMissingDataException::new);
    }
}
