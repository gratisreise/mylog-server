package com.mylog.domain.member.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberReader {
  private final MemberRepository memberRepository;

  public Member getById(Long memberId) {
    return memberRepository
        .findById(memberId)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
  }

  public Member getByNickname(String author) {
    return memberRepository
        .findByNickname(author)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
  }

  public Member getByEmail(String email) {
    return memberRepository
        .findByEmail(email)
        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
  }

  public boolean existsByProviderId(String providerId) {
    return memberRepository.existsByProviderId(providerId);
  }

  public boolean existsByEmail(String email) {
    return memberRepository.existsByEmail(email);
  }
}
