<<<<<<<< HEAD:src/main/java/com/mylog/domain/notification/repository/NotificationRepositoryCustom.java
package com.mylog.domain.notification.repository;

import com.mylog.domain.member.Member;
import com.mylog.domain.notification.Notification;
========
package com.mylog.notification.repository;

import com.mylog.member.entity.Member;
import com.mylog.notification.entity.Notification;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/notification/repository/NotificationRepositoryCustom.java
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom  {

    Page<Notification> findByMemberAndRead(Member member, Pageable pageable);
}
