package com.mylog.notification.repository;

import com.mylog.member.entity.Member;
import com.mylog.notification.entity.NotificationSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long>{

    Optional<NotificationSetting> findByMemberIdAndType(Long memberId, String type);
    boolean existsByMemberAndType(Member member, String type);
    List<NotificationSetting> findByMemberId(Long memberId);
}
