package com.mylog.domain.notificationsetting.service;

import com.mylog.domain.notificationsetting.repository.NotificationSettingRepository;
import com.mylog.common.exception.CMissingDataException;
import com.mylog.common.security.CustomUser;
import com.mylog.domain.notification.dto.NotificationSettingResponse;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.service.MemberReader;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingReader {

    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberReader memberReader;

    public boolean isDisabled(Member member, String type) {
        return notificationSettingRepository.findByMemberAndType(member, type)
            .orElseThrow(CMissingDataException::new)
            .isDisabled();
    }

    public List<NotificationSettingResponse> getNotificationSettings(CustomUser customUser){
        Member member = memberReader.getById(customUser.getMemberId());
        return notificationSettingRepository.findByMember(member)
            .stream().map(NotificationSettingResponse::new).toList();
    }

}
