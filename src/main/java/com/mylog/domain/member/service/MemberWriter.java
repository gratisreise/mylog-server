package com.mylog.domain.member.service;

import com.mylog.domain.member.dto.UpdateMemberRequest;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional
@RequiredArgsConstructor
public class MemberWriter {
  private final MemberRepository memberRepository;

  public void update(Member member, UpdateMemberRequest request, String imageUrl) {
    member.updateProfile(request.nickname(), request.bio(), imageUrl);
  }

  public void deleteById(Long memberId) {
    memberRepository.deleteById(memberId);
  }

  public void save(Member member) {
    memberRepository.save(member);
  }

  public Member saveOrUpdate(Member entity) {
    return memberRepository
        .findByProviderAndProviderId(entity.getProvider(), entity.getProviderId())
        .orElseGet(() -> memberRepository.save(entity));
  }
}
