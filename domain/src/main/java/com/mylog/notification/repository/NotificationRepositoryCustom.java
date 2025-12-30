package com.mylog.notification.repository;

import com.mylog.member.entity.Member;
import com.mylog.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom  {

    Page<Notification> findByMemberAndRead(Member member, Pageable pageable);
}
