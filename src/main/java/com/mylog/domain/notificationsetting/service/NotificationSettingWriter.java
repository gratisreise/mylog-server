package com.mylog.domain.notificationsetting.service;

import com.mylog.domain.notificationsetting.NotificationSetting;
import com.mylog.domain.notificationsetting.repository.NotificationSettingRepository;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.common.security.CustomUser;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.service.MemberReader;
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
