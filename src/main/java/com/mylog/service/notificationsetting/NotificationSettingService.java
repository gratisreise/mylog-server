package com.mylog.service.notificationsetting;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.notification.NotificationSettingResponse;
import com.mylog.model.entity.Member;
import com.mylog.repository.notificationsetting.NotificationSettingRepository;
import com.mylog.service.member.MemberReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {
    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberReader memberReader;

    public List<NotificationSettingResponse> getNotificationSettings(CustomUser customUser){
        Member member = memberReader.getById(customUser.getMemberId());
        return notificationSettingRepository.findByMember(member)
            .stream().map(NotificationSettingResponse::new).toList();
    }



}
