package com.mylog.repository.notification;

import com.mylog.model.entity.Member;
import com.mylog.model.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(
            """
            SELECT n FROM Notification n WHERE n.member = :member AND n.read = false
            AND n.type IN (SELECT ns.type FROM NotificationSetting ns WHERE ns.member = :member AND ns.disabled = false)
            """)
    Page<Notification> findAllByMemberAndReadFalse(Member member, Pageable pageable);

}
