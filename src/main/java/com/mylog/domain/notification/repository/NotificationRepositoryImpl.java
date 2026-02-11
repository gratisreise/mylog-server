package com.mylog.domain.notification.repository;

import com.mylog.domain.member.Member;
import com.mylog.domain.notification.Notification;
import com.mylog.model.entity.QNotification;
import com.mylog.model.entity.QNotificationSetting;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Notification> findByMemberAndRead(Member member, Pageable pageable) {
        QNotification notification = QNotification.notification;
        QNotificationSetting notificationSetting = QNotificationSetting.notificationSetting;

        List<Notification> content = queryFactory
            .select(notification)
            .from(notification, notificationSetting)
            .leftJoin(notification).on(notification.member.eq(notificationSetting.member))
            .where(notification.member.eq(member))
            .where(notificationSetting.disabled.isFalse())
            .where(notification.read.isFalse())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total =  queryFactory
            .select(notification.count())
            .from(notification, notificationSetting)
            .leftJoin(notification).on(notification.member.eq(notificationSetting.member))
            .where(notificationSetting.disabled.isFalse())
            .where(notification.read.isFalse())
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
