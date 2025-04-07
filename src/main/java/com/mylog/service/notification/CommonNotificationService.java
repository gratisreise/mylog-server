package com.mylog.service.notification;

import com.mylog.entity.Member;
import com.mylog.entity.Notification;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import com.mylog.repository.NotificationRepository;
import com.mylog.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommonNotificationService {
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void sendNotification(Long memberId, Long relatedId, String type) {
        Member member = memberRepository.findById(memberId)
            .orElseThrow(CMissingDataException::new);

        Notification notification = Notification.builder()
            .member(member)
            .type(type)
            .relatedId(relatedId)
            .build();

        notificationRepository.save(notification);
    }

    @Transactional
    public void readNotification(Long notificationId){
        Notification notification = notificationRepository.findById(notificationId)
            .orElseThrow(CMissingDataException::new);
        notification.makeRead();
    };
}
