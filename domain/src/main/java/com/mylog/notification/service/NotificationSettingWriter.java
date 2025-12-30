package com.mylog.notification.service;

import com.mylog.notification.entity.NotificationSetting;
import com.mylog.notification.repository.NotificationSettingRepository;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.api.auth.CustomUser;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingWriter {
    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberReader memberReader;


    @Async
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

    //알림끄기
    public void toggleNotification(CustomUser customUser, String type){
        Member member = memberReader.getByCustomUser(customUser);
        notificationSettingRepository
            .findByMemberAndType(member, type)
            .orElseThrow(CMissingDataException::new)
            .toggle();
    };



}
