package com.mylog.domain.member.repository;

import com.mylog.domain.member.entity.CustomWritingStyle;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomWritingStyleRepository extends JpaRepository<CustomWritingStyle, Long> {

  List<CustomWritingStyle> findByMemberIdOrderByCreatedAtDesc(Long memberId);

  Optional<CustomWritingStyle> findByIdAndMemberId(Long id, Long memberId);

  long countByMemberId(Long memberId);

  boolean existsByMemberIdAndName(Long memberId, String name);
}
