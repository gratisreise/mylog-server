package com.mylog.service.notificationsetting;

import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.notification.NotificationSettingResponse;
import com.mylog.model.entity.Member;
import com.mylog.repository.notificationsetting.NotificationSettingRepository;
import com.mylog.service.member.MemberReadService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {
    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberReadService memberReadService;

    public List<NotificationSettingResponse> getNotificationSettings(CustomUser customUser){
        Member member = memberReadService.getById(customUser.getMemberId());
        return notificationSettingRepository.findByMember(member)
            .stream().map(NotificationSettingResponse::new).toList();
    }



}
