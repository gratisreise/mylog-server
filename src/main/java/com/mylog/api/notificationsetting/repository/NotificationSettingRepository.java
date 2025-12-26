package com.mylog.api.notificationsetting.repository;

import com.mylog.api.member.entity.Member;
import com.mylog.api.notificationsetting.entity.NotificationSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long>{

    List<NotificationSetting> findByMember(Member member);
    Optional<NotificationSetting> findByMemberAndType(Member member, String type);
    boolean existsByMemberAndType(Member member, String type);
}
