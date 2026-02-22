<<<<<<<< HEAD:src/main/java/com/mylog/domain/notificationsetting/repository/NotificationSettingRepository.java
package com.mylog.domain.notificationsetting.repository;

import com.mylog.domain.member.Member;
import com.mylog.domain.notificationsetting.NotificationSetting;
========
package com.mylog.notification.repository;

import com.mylog.member.entity.Member;
import com.mylog.notification.entity.NotificationSetting;
>>>>>>>> origin/main:domain/src/main/java/com/mylog/notification/repository/NotificationSettingRepository.java
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long>{

    Optional<NotificationSetting> findByMemberIdAndType(Long memberId, String type);
    boolean existsByMemberAndType(Member member, String type);
    boolean existsByMember(Member member);
    List<NotificationSetting> findByMemberId(Long memberId);
}
