package com.mylog.service;

import com.mylog.exception.CMissingDataException;
import com.mylog.model.dto.classes.CustomUser;
import com.mylog.model.dto.notification.NotificationSettingResponse;
import com.mylog.model.entity.Member;
import com.mylog.repository.NotificationSettingRepository;
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
