package com.mylog.domain.member.repository;

import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.entity.NotificationSetting;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    Optional<NotificationSetting> findByMemberIdAndType(Long memberId, String type);
    boolean existsByMemberAndType(Member member, String type);
    boolean existsByMember(Member member);
    List<NotificationSetting> findByMemberId(Long memberId);
}
