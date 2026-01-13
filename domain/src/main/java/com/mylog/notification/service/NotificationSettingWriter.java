package com.mylog.notification.service;

import com.mylog.exception.common.CommonError;
import com.mylog.notification.entity.NotificationSetting;
import com.mylog.notification.repository.NotificationSettingRepository;

import com.mylog.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.mylog.exception.common.CMissingDataException;
import com.mylog.exception.CMissingDataException;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingWriter {
    private final NotificationSettingRepository notificationSettingRepository;
    private static String[] TYPES = {"comment"};

    @Async
    public void createNotificationSetting(Member member){
        if(notificationSettingRepository.existsByMember(member)){
            return;
        }

        for(String type : TYPES){
            NotificationSetting setting = NotificationSetting.createDefault(member, type);
            notificationSettingRepository.save(setting);
        }

    }

    public void toggleNotification(Long memberId, String type) {
        notificationSettingRepository
            .findByMemberIdAndType(memberId, type)
            .orElseThrow(() -> new CMissingDataException(CommonError.NOTIFICATION_SETTING_IS_EMPTY))
            .toggle();
    }

}
