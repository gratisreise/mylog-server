package com.mylog.repository.notification;

import com.mylog.model.entity.Member;
import com.mylog.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom  {

    Page<Notification> findByMemberAndRead(Member member, Pageable pageable);
}
