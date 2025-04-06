package com.mylog.service.notification;

import com.mylog.enums.OauthProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationServiceFactory {

    private final LocalNotificationService localNotificationService;
    private final SocialNotificationService socialNotificationService;

    public NotificationService getNotificationService(OauthProvider oauthProvider) {
        return oauthProvider == OauthProvider.LOCAL ?
            localNotificationService :
            socialNotificationService;
    }
}
