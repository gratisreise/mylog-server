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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@ServiceType(OauthProvider.LOCAL)
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocalNotificationService implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationSettingRepository notificationSettingRepository;
    private final MemberRepository memberRepository;

    /**알림 가져오기
     * 안 읽은 것
     * 메세지: 해당 내용
     */
    @Override
    public Page<NotificationResponse> receiveNotification(CustomUser customUser) {


        return null;
    }

    @Override
    @Transactional
    public void toggleNotification(CustomUser customUser, String type) {

    }
}
