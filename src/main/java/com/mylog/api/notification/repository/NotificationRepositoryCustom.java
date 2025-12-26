package com.mylog.api.notification.repository;

import com.mylog.api.member.entity.Member;
import com.mylog.api.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom  {

    Page<Notification> findByMemberAndRead(Member member, Pageable pageable);
}
