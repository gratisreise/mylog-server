package com.mylog.notification.service;


import com.mylog.exception.common.CMissingDataException;
import com.mylog.notification.entity.Notification;
import com.mylog.notification.repository.NotificationRepository;
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

    public Notification getById(long notificationId){
        return notificationRepository.findById(notificationId)
            .orElseThrow(CMissingDataException::new);
    }

    //알림목록조회
    public Page<Notification> receiveNotification(Long memberId, Pageable pageable){
        return notificationRepository.findAllByMemberIdAndReadFalse(memberId, pageable);
    };

}
