package com.mylog.service.notification;

import com.mylog.annotations.ServiceType;
import com.mylog.dto.classes.CustomUser;
import com.mylog.dto.notification.NotificationResponse;
import com.mylog.entity.Member;
import com.mylog.entity.Notification;
import com.mylog.enums.OauthProvider;
import com.mylog.exception.CMissingDataException;
import com.mylog.repository.MemberRepository;
import com.mylog.repository.NotificationRepository;
import com.mylog.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ServiceType(OauthProvider.SOCIAL)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialNotificationService implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional
    public Page<NotificationResponse> receiveNotification(
        CustomUser customUser, Pageable pageable) {
            Member member = generateMember(customUser);

            return notificationRepository
                .findAllByMemberAndReadFalse(member, pageable)
                .map(NotificationResponse::from);
    }


    @Override
    @Transactional
    public void toggleNotification(CustomUser customUser, String type) {
        Member member = generateMember(customUser);
        notificationSettingRepository
            .findByMemberAndType(member, type)
            .orElseThrow(CMissingDataException::new)
            .toggle();
    }

    private Member generateMember(CustomUser customUser) {
        return memberRepository
            .findById(Long.valueOf(customUser.getUsername()))
            .orElseThrow(CMissingDataException::new);
    }
}
