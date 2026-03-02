package com.mylog.domain.member.service;

import com.mylog.common.exception.CMissingDataException;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.entity.NotificationSetting;
import com.mylog.domain.member.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    //알림끄기
    public void toggleNotification(Long memberId, String type){
        notificationSettingRepository
            .findByMemberIdAndType(memberId, type)
            .orElseThrow(CMissingDataException::new)
            .toggle();
    }

}
