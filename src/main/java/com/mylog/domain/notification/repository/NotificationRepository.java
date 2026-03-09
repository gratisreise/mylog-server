package com.mylog.domain.notification.repository;

import com.mylog.domain.member.entity.Member;
import com.mylog.domain.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

  Page<Notification> findByMemberAndReadTrue(Member member, Pageable pageable);
}
