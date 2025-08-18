package com.mylog.repository.notificationsetting;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class NotificationSettingRepositoryImpl implements NotificationSettingRepositoryCustom {
    private final JPAQueryFactory queryFactory;
}
