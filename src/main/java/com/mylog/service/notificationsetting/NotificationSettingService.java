package com.mylog.service.notificationsetting;

import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.entity.Member;
import com.mylog.model.entity.NotificationSetting;
import com.mylog.repository.notificationsetting.NotificationSettingRepository;
import com.mylog.service.member.MemberReader;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationSettingService {
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
