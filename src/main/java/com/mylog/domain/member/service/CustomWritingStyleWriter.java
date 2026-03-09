package com.mylog.domain.member.service;

import com.mylog.common.exception.BusinessException;
import com.mylog.common.exception.ErrorCode;
import com.mylog.domain.member.entity.CustomWritingStyle;
import com.mylog.domain.member.entity.Member;
import com.mylog.domain.member.repository.CustomWritingStyleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CustomWritingStyleWriter {

  public static final int MAX_STYLES_PER_MEMBER = 3;

  private final CustomWritingStyleRepository customWritingStyleRepository;
  private final MemberReader memberReader;

  public CustomWritingStyle create(Long memberId, String name, String role, String instruction) {
    // 개수 제한 검증
    long count = customWritingStyleRepository.countByMemberId(memberId);
    if (count >= MAX_STYLES_PER_MEMBER) {
      throw new BusinessException(ErrorCode.CUSTOM_STYLE_LIMIT_REACHED);
    }

    // 이름 중복 검증
    if (customWritingStyleRepository.existsByMemberIdAndName(memberId, name)) {
      throw new BusinessException(ErrorCode.CUSTOM_STYLE_NAME_DUPLICATED);
    }

    Member member = memberReader.getById(memberId);
    CustomWritingStyle style = CustomWritingStyle.create(member, name, role, instruction);
    return customWritingStyleRepository.save(style);
  }

  public void update(Long styleId, Long memberId, String name, String role, String instruction) {
    CustomWritingStyle style = getByIdAndValidateOwner(styleId, memberId);

    // 이름 변경 시 중복 검증
    if (name != null && !name.equals(style.getName())) {
      if (customWritingStyleRepository.existsByMemberIdAndName(memberId, name)) {
        throw new BusinessException(ErrorCode.CUSTOM_STYLE_NAME_DUPLICATED);
      }
    }

    style.update(name, role, instruction);
  }

  public void delete(Long styleId, Long memberId) {
    CustomWritingStyle style = getByIdAndValidateOwner(styleId, memberId);
    customWritingStyleRepository.delete(style);
  }

  private CustomWritingStyle getByIdAndValidateOwner(Long styleId, Long memberId) {
    CustomWritingStyle style = customWritingStyleRepository
        .findById(styleId)
        .orElseThrow(() -> new BusinessException(ErrorCode.CUSTOM_STYLE_NOT_FOUND));

    if (!style.getMember().getId().equals(memberId)) {
      throw new BusinessException(ErrorCode.CUSTOM_STYLE_FORBIDDEN);
    }

    return style;
  }
}
