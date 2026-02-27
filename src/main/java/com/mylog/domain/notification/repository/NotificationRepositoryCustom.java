package com.mylog.domain.notification.repository;

import com.mylog.domain.member.Member;
import com.mylog.domain.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom {

    Page<Notification> findByMemberAndRead(Member member, Pageable pageable);
}
