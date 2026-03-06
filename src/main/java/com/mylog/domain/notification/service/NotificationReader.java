package com.mylog.domain.notification.service;

import com.mylog.common.exception.CMissingDataException;
import com.mylog.common.security.CustomUser;
import com.mylog.domain.member.Member;
import com.mylog.domain.member.service.MemberReader;
import com.mylog.domain.notification.Notification;
import com.mylog.domain.notification.dto.NotificationResponse;
import com.mylog.domain.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationReader {
    private final NotificationRepository notificationRepository;
    private final MemberReader memberReader;

    public Notification getById(long notificationId){
        return notificationRepository.findById(notificationId)
            .orElseThrow(CMissingDataException::new);
    }

    //알림목록조회
    public Page<NotificationResponse> receiveNotification(CustomUser customUser, Pageable pageable){
        Member member = memberReader.getByCustomUser(customUser);
        return notificationRepository
            .findByMemberAndRead(member, pageable)
            .map(NotificationResponse::from);
    };

}
