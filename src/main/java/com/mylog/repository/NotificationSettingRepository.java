package com.mylog.repository;

import com.mylog.entity.Member;
import com.mylog.entity.NotificationSetting;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByMemberAndType(Member member, String type);
    boolean existsByMemberAndType(Member member, String type);
}
