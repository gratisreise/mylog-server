package com.mylog.notification.service;

import com.mylog.notification.entity.NotificationSetting;
import com.mylog.notification.repository.NotificationSettingRepository;

import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mylog.exception.CMissingDataException;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingWriter {
    private final NotificationSettingRepository notificationSettingRepository;

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

    public void toggleNotification(Long memberId, String type) {
        notificationSettingRepository
            .findByMemberIdAndType(memberId, type)
            .orElseThrow(CMissingDataException::new)
            .toggle();
    }




}
