package com.mylog.notification.service;

import com.mylog.notification.repository.NotificationSettingRepository;
import com.mylog.member.entity.Member;
import com.mylog.member.service.MemberReader;
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

//    public boolean isDisabled(Member member, String type) {
//        return notificationSettingRepository.findByMemberAndType(member, type)
//            .orElseThrow(CMissingDataException::new)
//            .isDisabled();
//    }
//
//    public List<NotificationSettingResponse> getNotificationSettings(CustomUser customUser){
//        Member member = memberReader.getById(customUser.getMemberId());
//        return notificationSettingRepository.findByMember(member)
//            .stream().map(NotificationSettingResponse::new).toList();
//    }

}
