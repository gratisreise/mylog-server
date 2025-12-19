package com.mylog.api.notification;

import com.mylog.domain.entity.Member;
import com.mylog.domain.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom  {

    Page<Notification> findByMemberAndRead(Member member, Pageable pageable);
}
