package com.mylog.domain.member.service;

import com.mylog.domain.category.service.CategoryWriter;
import com.mylog.domain.member.dto.UpdateMemberRequest;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.repository.MemberRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class MemberWriter {
  private final MemberRepository memberRepository;
  private final CategoryWriter categoryWriter;
  private final NotificationSettingWriter notificationSettingWriter;

  public void update(Member member, UpdateMemberRequest request, String imageUrl) {
    member.updateProfile(request.nickname(), request.bio(), imageUrl);
  }

  public void deleteById(Long memberId) {
    memberRepository.deleteById(memberId);
  }

  public void save(Member member) {
    memberRepository.save(member);
    initializeMemberData(member);
  }

  public Member saveOrUpdate(Member entity) {
    Optional<Member> existing =
        memberRepository.findByProviderAndProviderId(entity.getProvider(), entity.getProviderId());
    if (existing.isPresent()) {
      return existing.get();
    }
    Member saved = memberRepository.save(entity);
    initializeMemberData(saved);
    return saved;
  }

  private void initializeMemberData(Member member) {
    categoryWriter.createCategory(member);
    notificationSettingWriter.createNotificationSetting(member, "comment");
  }
}
